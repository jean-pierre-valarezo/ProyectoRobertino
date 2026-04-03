package ups.logic.robbyapp.configuraciones

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ups.logic.robbyapp.PromptRequest
import ups.logic.robbyapp.ResponseText


interface ApiService {
    @Multipart
    @POST("process_img")
    fun uploadFrame(@Part frame: MultipartBody.Part): Call<ResponseBody>

    ///////////////////////Metodos individuales

    @Multipart
    @POST("aacuaticos")
    fun aacuatico(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("aaereos")
    fun aaereo(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("aterrestres")
    fun aterrestre(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("taereos")
    fun taereo(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("taacuaticos")
    fun taacuatico(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("tterrestres")
    fun tterrestre(@Part frame: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @POST("comidachatarra")
    fun comidachatarra(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("emocion")
    fun emocion(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("fruta")
    fun fruta   (@Part frame: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @POST("higiene")
    fun higiene(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("instrumentomusical")
    fun instrumentomusical(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("juguete")
    fun juguete(@Part frame: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @POST("numero")
    fun numero(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("persona")
    fun persona(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("prendadevestir")
    fun prendadevestir(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("verdura")
    fun verdura(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("colores")
    fun colores(@Part frame: MultipartBody.Part): Call<ResponseBody>

    ///////////////////////

    @Multipart
    @POST("process_img_especifica")
    fun uploadFrameEspecifico(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("process_numero")
    fun process_numero(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("process_letra")
    fun process_letra(@Part frame: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("process_vocal")
    fun process_vocal(@Part frame: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @POST("upload_audio")
    fun process_audio(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("ollama")
    fun sendPrompt(@Body prompt: PromptRequest): Call<ResponseText>

    @GET("word")
    fun words(): Call<ResponseBody>

}