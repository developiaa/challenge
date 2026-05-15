package pro.developia._2026_05_02


import org.springframework.stereotype.Service
import kotlinx.coroutines.delay
import pro.developia.grpc.chat.ChatLogicServiceGrpcKt
import pro.developia.grpc.chat.SendMessageRequest
import pro.developia.grpc.chat.SendMessageResponse
import java.util.UUID

@Service
class ChatLogicServiceImpl : ChatLogicServiceGrpcKt.ChatLogicServiceCoroutineImplBase() {

    override suspend fun sendMessage(request: SendMessageRequest): SendMessageResponse {
        println("웹소켓 서버로부터 메시지 수신: [Room: ${request.roomId}] ${request.userId} - ${request.content}")

        delay(50)

        return SendMessageResponse.newBuilder()
            .setSuccess(true)
            .setMessageId(UUID.randomUUID().toString())
            .setTimestamp(System.currentTimeMillis())
            .build()
    }
}
