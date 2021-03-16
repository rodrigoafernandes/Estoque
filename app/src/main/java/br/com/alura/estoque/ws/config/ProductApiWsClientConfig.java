package br.com.alura.estoque.ws.config;

import br.com.alura.estoque.ws.client.ProductApiWsClient;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductApiWsClientConfig {

    private final Retrofit retrofit;

    public ProductApiWsClientConfig() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        retrofit  = new Retrofit.Builder()
                .baseUrl("http://192.168.0.11:8080")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public ProductApiWsClient getClient() {
        return retrofit.create(ProductApiWsClient.class);
    }

}
