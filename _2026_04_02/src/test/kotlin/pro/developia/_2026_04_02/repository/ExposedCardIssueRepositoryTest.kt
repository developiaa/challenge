package pro.developia._2026_04_02.repository

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_04_02.domain.IssueStatus
import kotlin.test.Test

@SpringBootTest
@Transactional
class ExposedCardIssueRepositoryTest {
    @Autowired
    private lateinit var repository: ExposedCardIssueRepository

    @Test
    fun `Exposed CRUD가 정상적으로 동작한다`() {
        // 1. CREATE 테스트
        val userId = 1004L
        val issueId = repository.createIssue(userId)

        issueId.shouldNotBeNull()

        // 2. READ 테스트
        val savedIssue = repository.findById(issueId)

        savedIssue.shouldNotBeNull()
        savedIssue.userId shouldBe userId
        savedIssue.status shouldBe IssueStatus.PENDING

        // 3. UPDATE 테스트
        val isUpdated = repository.updateStatus(issueId, IssueStatus.SUCCESS)
        isUpdated shouldBe true

        val updatedIssue = repository.findById(issueId)
        updatedIssue?.status shouldBe IssueStatus.SUCCESS

        // 4. DELETE 테스트
        val isDeleted = repository.deleteById(issueId)
        isDeleted shouldBe true

        val deletedIssue = repository.findById(issueId)
        deletedIssue.shouldBeNull()
    }

}
