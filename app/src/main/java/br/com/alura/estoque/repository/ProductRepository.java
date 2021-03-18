package br.com.alura.estoque.repository;

import android.content.Context;

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
import retrofit2.internal.EverythingIsNonNull;

public class ProductRepository {

    public static final String LOCATION_HEADER = "Location";
    private final ProdutoDAO dao;
    private final ProductApiWsClient client = new ProductApiWsClientConfig().getClient();

    public ProductRepository(Context context) {
        this.dao = EstoqueDatabase.getInstance(context).getProdutoDAO();
    }

    public void buscaProdutos(ProductsMethodsCallback<List<Produto>> callback) {
        findProductsDB(callback);
    }

    public void salva(Produto produto, ProductsMethodsCallback<Produto> callback) {
        saveOnAPI(produto, callback);
    }

    public void edita(Produto produto, ProductsMethodsCallback<Produto> callback) {
        updateOnAPI(produto, callback);
    }

    public void remove(Produto produto, ProductsMethodsCallback<Void> callback) {
        deleteOnAPI(produto, callback);
    }

    private void deleteOnAPI(Produto produto, ProductsMethodsCallback<Void> callback) {
        Call<Void> call = client.delete(produto.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    removeOnDB(produto, callback);
                } else {
                    callback.onFail(String.format("Error deleting product %d on API. %d", produto.getId(), response.code()));
                }

            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFail(String.format("Error calling products API: %s", t.getMessage()));
            }
        });
    }

    private void removeOnDB(Produto produto, ProductsMethodsCallback<Void> callback) {
        new TaskRunner().executeAsync(() -> {
            dao.remove(produto);
            return null;
        }, result -> callback.onSuccess(null));
    }

    private void updateOnAPI(Produto produto, ProductsMethodsCallback<Produto> callback) {
        Call<Void> call = client.update(produto.getId(), produto);

        call.enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    updateProductOnDB(produto, callback);

                } else {
                    callback.onFail(String.format("Error updating product %d on API: %d", produto.getId(), response.code()));
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFail(String.format("Error calling products API: %s", t.getMessage()));
            }
        });
    }

    private void updateProductOnDB(Produto produto, ProductsMethodsCallback<Produto> callback) {
        new TaskRunner().executeAsync(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::onSuccess);
    }

    private void saveOnAPI(Produto produto, ProductsMethodsCallback<Produto> callback) {
        Call<Void> call = client.save(produto);

        call.enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    Map<String, List<String>> headers = response.headers().toMultimap();
                    
                    if (headers.containsKey(LOCATION_HEADER)) {
                        String location = headers.get(LOCATION_HEADER).get(0);
                        long generatedId = Long.parseLong(
                                location.substring(location.lastIndexOf("/") + 1));

                        saveOnDB(generatedId, produto, callback);
                    }

                }
            }

            @Override
            @EverythingIsNonNull
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

    private void findProductsDB(ProductsMethodsCallback<List<Produto>> callback) {
        new TaskRunner().executeAsync(() -> dao.buscaTodos(), result -> {
            callback.onSuccess(result);
            findProductsOnApi(callback);
        });
    }

    private void findProductsOnApi(ProductsMethodsCallback<List<Produto>> callback) {
        Call<List<Produto>> call = client.findAll();

        call.enqueue(new Callback<List<Produto>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (response.isSuccessful()) {
                    List<Produto> produtos = response.body();
                    updateProductsDB(produtos, callback);
                } else {
                    callback.onFail(String.format("Error getting products from API: %d", response.code()));
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callback.onFail(String.format("Error calling products API: $s", t.getMessage()));
            }
        });

    }

    private void updateProductsDB(List<Produto> produtos, ProductsMethodsCallback<List<Produto>> callback) {
        new TaskRunner().executeAsync(() -> {
            dao.salva(produtos);
            return dao.buscaTodos();
        }, callback::onSuccess);
    }

    public interface ProductsMethodsCallback <T> {
        void onSuccess(T result);
        void onFail(String error);
    }

}
