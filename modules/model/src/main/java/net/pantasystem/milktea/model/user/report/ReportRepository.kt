package net.pantasystem.milktea.model.user.report

interface ReportRepository {
    suspend fun create(report: Report): Result<Unit>
}