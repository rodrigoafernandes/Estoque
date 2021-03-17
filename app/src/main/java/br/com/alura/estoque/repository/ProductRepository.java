package br.com.alura.estoque.repository;

import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.com.alura.estoque.asynctask.TaskRunner;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.ws.client.ProductApiWsClient;
import br.com.alura.estoque.ws.config.ProductApiWsClientConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    public static final String LOCATION_HEADER = "Location";
    private final ProdutoDAO dao;
    private final ProductApiWsClient client = new ProductApiWsClientConfig().getClient();

    public ProductRepository(Context context) {
        this.dao = EstoqueDatabase.getInstance(context).getProdutoDAO();
    }

    public void buscaProdutos(ProductsMethodsListener<List<Produto>> listener) {
        findProductsDB(listener);
    }

    public void salva(Produto produto, ProductsMethodsCallback<Produto> callback) {
        saveOnAPI(produto, callback);
    }

    private void saveOnAPI(Produto produto, ProductsMethodsCallback<Produto> callback) {
        Call<Void> call = client.save(produto);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    Map<String, List<String>> headers = response.headers().toMultimap();
                    
                    if (headers.containsKey(LOCATION_HEADER)) {
                        String location = headers.get(LOCATION_HEADER).get(0);
                        long generatedId = Long.valueOf(
                                location.substring(location.lastIndexOf("/") + 1));

                        saveOnDB(generatedId, produto, callback);
                    }

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFail(String.format("Error to call API. %s", t.getMessage()));
            }
        });
    }

    private void saveOnDB(long generatedId, Produto produto, ProductsMethodsCallback<Produto> callback) {
        new TaskRunner().executeAsync(() -> {
            produto.setId(generatedId);
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, callback::onSuccess);
    }

    private void findProductsDB(ProductsMethodsListener<List<Produto>> listener) {
        new TaskRunner().executeAsync(() -> dao.buscaTodos(), result -> {
            listener.whenProductsIsLoaded(result);
            findProductsOnApi(listener);
        });
    }

    private void findProductsOnApi(ProductsMethodsListener<List<Produto>> listener) {
        Call<List<Produto>> call = client.findAll();

        new TaskRunner().executeAsync(() -> {
            try {
                Response<List<Produto>> response = call.execute();

                List<Produto> products = response.body();

                dao.salva(products);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dao.buscaTodos();
        }, listener::whenProductsIsLoaded);
    }

    public interface ProductsMethodsListener <T> {
        void whenProductsIsLoaded(T products);
    }

    public interface ProductsMethodsCallback <T> {
        void onSuccess(T result);
        void onFail(String error);
    }

}
