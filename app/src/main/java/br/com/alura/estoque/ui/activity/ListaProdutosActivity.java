package br.com.alura.estoque.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProductRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProductRepository repository;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraBotaoLoginFacebook();
        configuraListaProdutos();
        configuraFabSalvaProduto();

        repository = new ProductRepository(this);
        repository.buscaProdutos(new ProductRepository.ProductsMethodsCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> result) {
                adapter.atualiza(result);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(ListaProdutosActivity.this, "Error getting products",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void configuraBotaoLoginFacebook() {
        configuraCallBackManager();
        LoginButton loginButton = findViewById(R.id.login_button);

        loginButton.setPermissions("email");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(ListaProdutosActivity.this, "Logado", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(ListaProdutosActivity.this, "Cancelou o login", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ListaProdutosActivity.this, "Erro ao logar", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configuraCallBackManager() {
        callbackManager = CallbackManager.Factory.create();
    }

    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener(((posicao, produtoRemovido) ->
            repository.remove(produtoRemovido, new ProductRepository.ProductsMethodsCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    adapter.remove(posicao);
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(ListaProdutosActivity.this,
                            "Não foi possível remover o produto. " + error,
                            Toast.LENGTH_LONG).show();
                }
            })
        ));
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, produto ->
                repository.salva(produto, new ProductRepository.ProductsMethodsCallback<Produto>() {
                    @Override
                    public void onSuccess(Produto result) {
                        adapter.adiciona(result);
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(ListaProdutosActivity.this,
                                "Não foi possível salvar o produto", Toast.LENGTH_LONG).show();
                    }
                }))
                .mostra();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoEditado -> repository.edita(produtoEditado, new ProductRepository.ProductsMethodsCallback<Produto>() {
                    @Override
                    public void onSuccess(Produto result) {
                        adapter.edita(posicao, result);
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(ListaProdutosActivity.this,
                                "Não foi possível atualizar o produto. " + error,
                                Toast.LENGTH_LONG).show();
                    }
                }))
                .mostra();
    }


}
