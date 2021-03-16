package br.com.alura.estoque.asynctask;

import java.util.concurrent.Callable;

public interface AsyncTask<T> extends Callable<T> {

    @Override
    T call();
}
