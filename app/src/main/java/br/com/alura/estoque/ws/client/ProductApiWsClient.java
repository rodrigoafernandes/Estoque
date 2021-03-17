package br.com.alura.estoque.ws.client;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ProductApiWsClient {

    @GET("/products")
    Call<List<Produto>> findAll();

    @POST("/products")
    Call<Void> save(@Body Produto produto);

}
