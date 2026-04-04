package pro.developia._2026_04_02.repository

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
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
    val userId = 1004L
    var id: Long? = null

    @BeforeEach
    fun setUp() {
        id = repository.createIssue(userId)
    }

    @Test
    fun create() {
        val issueId = repository.createIssue(userId)

        issueId.shouldNotBeNull()
    }

    @Test
    fun read() {
        val savedIssue = repository.findById(id!!)

        savedIssue.shouldNotBeNull()
        savedIssue.userId shouldBe userId
        savedIssue.status shouldBe IssueStatus.PENDING
    }

    @Test
    fun update() {
        val isUpdated = repository.updateStatus(id!!, IssueStatus.SUCCESS)
        isUpdated shouldBe true

        val updatedIssue = repository.findById(id!!)
        updatedIssue?.status shouldBe IssueStatus.SUCCESS
    }

    @Test
    fun delete() {
        val isDeleted = repository.deleteById(id!!)
        isDeleted shouldBe true

        val deletedIssue = repository.findById(id!!)
        deletedIssue.shouldBeNull()
    }

}
