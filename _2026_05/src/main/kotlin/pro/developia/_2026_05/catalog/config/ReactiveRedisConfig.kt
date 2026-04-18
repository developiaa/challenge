package pro.developia._2026_05.catalog.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import pro.developia._2026_05.catalog.domain.model.CatalogItem

@Configuration
class ReactiveRedisConfig {

    @Bean
    fun reactiveCatalogRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, CatalogItem> {
        val objectMapper: ObjectMapper = jacksonObjectMapper()
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, CatalogItem::class.java)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, CatalogItem>(StringRedisSerializer()) // Key는 String
            .value(valueSerializer)
            .hashKey(StringRedisSerializer())
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}
