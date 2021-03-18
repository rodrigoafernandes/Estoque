package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProductRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProductRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        EstoqueDatabase db = EstoqueDatabase.getInstance(this);

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

    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener(((posicao, produtoRemovido) -> {
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
            });
        }));
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
