package pro.developia._2026_02.annotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import pro.developia._2026_02.config.ShardingContextHolder;
import pro.developia._2026_02.strategy.ShardingStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Aspect
@Component
@Order(1) // 트랜잭션 AOP보다 먼저 실행되어야 함 (LazyProxy 사용 시엔 덜 민감하지만 안전장치)
public class ShardingAspect {
    private final Map<String, ShardingStrategy> strategyMap;
    //    private final ShardingStrategy shardingStrategy;
    private final ExpressionParser parser = new SpelExpressionParser();

    public ShardingAspect(List<ShardingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getAnnotation(Component.class).value().toUpperCase(),
                        s -> s
                ));
    }


    @Around("@annotation(sharding)")
    public Object distribute(ProceedingJoinPoint joinPoint, Sharding sharding) throws Throwable {
        String strategyKey = sharding.strategy().name(); // HASH or RANGE
        ShardingStrategy strategy = strategyMap.get(strategyKey);

        if (strategy == null) {
            throw new IllegalArgumentException("Unknown sharding strategy: " + strategyKey);
        }

        // SpEL을 사용하여 파라미터 값 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        Object shardKey = parser.parseExpression(sharding.key()).getValue(context);

        // 타겟 DB 결정 및 스레드 로컬 설정
        String targetDb = strategy.getTargetKey(shardKey);
        ShardingContextHolder.setKey(targetDb);

        try {
            return joinPoint.proceed();
        } finally {
            ShardingContextHolder.clear();
        }
    }
}
