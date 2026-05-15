package pro.developia._2026_05_02

import io.grpc.StatusException
import org.springframework.stereotype.Component
import pro.developia.grpc.chat.ChatLogicServiceGrpcKt
import pro.developia.grpc.chat.SendMessageRequest

@Component
class ChatWebSocketHandler(
    // 스프링이 관리하는 Coroutine Stub을 바로 주입받습니다.
    private val stub: ChatLogicServiceGrpcKt.ChatLogicServiceCoroutineStub
) {

    suspend fun forwardMessageToLogicServer(userId: String, roomId: String, content: String) {
        val request = SendMessageRequest.newBuilder()
            .apply {
                this.userId = userId
                this.roomId = roomId
                this.content = content
            }.build()

        try {
            val response = stub.sendMessage(request)
            println("로직 서버 응답: ${response.messageId}")
        } catch (e: StatusException) {
            println("gRPC 에러: ${e.status.code}")
        }
    }
}
