package pro.developia._2026_05_02

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.client.GrpcChannelFactory
import pro.developia.grpc.chat.ChatLogicServiceGrpcKt

@Configuration
class GrpcClientConfig {

    @Bean
    fun chatLogicCoroutineStub(channelFactory: GrpcChannelFactory): ChatLogicServiceGrpcKt.ChatLogicServiceCoroutineStub {
        // logic-server 채널 생성
        val channel = channelFactory.createChannel("logic-server")
        return ChatLogicServiceGrpcKt.ChatLogicServiceCoroutineStub(channel)
    }
}
