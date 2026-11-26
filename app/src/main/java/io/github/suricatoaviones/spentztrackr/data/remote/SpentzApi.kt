package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.data.remote.dto.ApiEnvelope
import io.github.suricatoaviones.spentztrackr.data.remote.dto.AuthTokenData
import io.github.suricatoaviones.spentztrackr.data.remote.dto.CategoriesListDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.CategoryRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.DashboardDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpensesListDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.IdDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.IncomeRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.IncomesListDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.LoginRequest
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ManualRateDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ManualRateRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ProfileRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.RatesDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.RegisterRequest
import io.github.suricatoaviones.spentztrackr.data.remote.dto.SourceRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.SourcesListDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Cliente de la API `/api/v1` del backend. La URL base es un placeholder:
 * [DynamicBaseUrlInterceptor] la reescribe con la que el usuario guardó en
 * settings (docs/05). Filtros según `CategoryController`/api-documentation.
 */
interface SpentzApi {
    // ---------- Auth ----------

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): ApiEnvelope<AuthTokenData>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): ApiEnvelope<AuthTokenData>

    @GET("auth/me")
    suspend fun me(): ApiEnvelope<UserDto>

    @DELETE("auth/logout")
    suspend fun logout(): ApiEnvelope<String?>

    /** Actualiza moneda de visualización y `tracking_type` (docs/05). */
    @PUT("user/profile")
    suspend fun updateProfile(
        @Body request: ProfileRequestDto,
    ): ApiEnvelope<String?>

    // ---------- Dashboard ----------

    @GET("dashboard")
    suspend fun getDashboard(): ApiEnvelope<DashboardDataDto>

    // ---------- Gastos ----------

    @GET("expenses")
    suspend fun listExpenses(
        @Query("search") search: String?,
        @Query("currency") currency: String?,
        @Query("category_id") categoryId: Long?,
        @Query("from") from: String?,
        @Query("to") to: String?,
    ): ApiEnvelope<ExpensesListDataDto>

    @POST("expenses")
    suspend fun createExpense(
        @Body request: ExpenseRequestDto,
    ): ApiEnvelope<IdDto>

    @GET("expenses/{id}")
    suspend fun getExpense(
        @Path("id") id: Long,
    ): ApiEnvelope<io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseDto>

    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Long,
        @Body request: ExpenseRequestDto,
    ): ApiEnvelope<IdDto>

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: Long,
    ): ApiEnvelope<IdDto>

    // ---------- Ingresos ----------

    @GET("incomes")
    suspend fun listIncomes(
        @Query("search") search: String?,
        @Query("currency") currency: String?,
        @Query("category_id") categoryId: Long?,
        @Query("from") from: String?,
        @Query("to") to: String?,
    ): ApiEnvelope<IncomesListDataDto>

    @POST("incomes")
    suspend fun createIncome(
        @Body request: IncomeRequestDto,
    ): ApiEnvelope<IdDto>

    @GET("incomes/{id}")
    suspend fun getIncome(
        @Path("id") id: Long,
    ): ApiEnvelope<io.github.suricatoaviones.spentztrackr.data.remote.dto.IncomeDto>

    @PUT("incomes/{id}")
    suspend fun updateIncome(
        @Path("id") id: Long,
        @Body request: IncomeRequestDto,
    ): ApiEnvelope<IdDto>

    @DELETE("incomes/{id}")
    suspend fun deleteIncome(
        @Path("id") id: Long,
    ): ApiEnvelope<IdDto>

    // ---------- Categorías ----------

    @GET("categories")
    suspend fun listCategories(): ApiEnvelope<CategoriesListDataDto>

    @POST("categories")
    suspend fun createCategory(
        @Body request: CategoryRequestDto,
    ): ApiEnvelope<IdDto>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body request: CategoryRequestDto,
    ): ApiEnvelope<IdDto>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long,
    ): ApiEnvelope<IdDto>

    // ---------- Orígenes de pago ----------

    @GET("sources")
    suspend fun listSources(): ApiEnvelope<SourcesListDataDto>

    @POST("sources")
    suspend fun createSource(
        @Body request: SourceRequestDto,
    ): ApiEnvelope<IdDto>

    @PUT("sources/{id}")
    suspend fun updateSource(
        @Path("id") id: Long,
        @Body request: SourceRequestDto,
    ): ApiEnvelope<IdDto>

    @DELETE("sources/{id}")
    suspend fun deleteSource(
        @Path("id") id: Long,
    ): ApiEnvelope<IdDto>

    // ---------- Tasas ----------

    @GET("rates")
    suspend fun getRates(): ApiEnvelope<RatesDataDto>

    @PUT("rates")
    suspend fun saveManualRate(
        @Body request: ManualRateRequestDto,
    ): ApiEnvelope<ManualRateDataDto>

    @POST("rates/sync")
    suspend fun syncRates(): ApiEnvelope<String?>
}
