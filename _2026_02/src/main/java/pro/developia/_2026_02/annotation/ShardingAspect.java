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


@Slf4j
@Aspect
@Component
@Order(1) // 트랜잭션 AOP보다 먼저 실행되어야 함 (LazyProxy 사용 시엔 덜 민감하지만 안전장치)
@RequiredArgsConstructor
public class ShardingAspect {
    private final ShardingStrategy shardingStrategy;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(sharding)")
    public Object distribute(ProceedingJoinPoint joinPoint, Sharding sharding) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String keyExpression = sharding.key(); // 예: "#productId"

        // 1. SpEL을 사용하여 파라미터 값 추출
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Object shardKey = parser.parseExpression(keyExpression).getValue(context);

        // 전략을 통해 타겟 DB 키 결정
        String targetDataSourceKey = shardingStrategy.getTargetKey(shardKey);

        // ContextHolder에 저장
        ShardingContextHolder.setKey(targetDataSourceKey);

        log.info("Sharding Key: {}, Target DB: {}", shardKey, targetDataSourceKey);

        try {
            return joinPoint.proceed();
        } finally {
            ShardingContextHolder.clear();
        }
    }
}
