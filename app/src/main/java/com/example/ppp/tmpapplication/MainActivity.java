package com.example.ppp.tmpapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable.just(createContainer(true))
                .subscribeOn(Schedulers.io())
                .lift(new ContainerCheck())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(container -> doSomethingWhenSuccess())
                .map(container -> container.books)
                .flatMap(Observable::from)
                .subscribe(book -> {
                    doSomethingForBook(book);
                }, e -> doSomethingWhenFail());

    }

    private void doSomethingWhenFail() {
        Log.d("RxLog", "doSomethingWhenFail");
    }

    private void doSomethingForBook(Book book) {
        Log.d("RxLog", "doSomethingForBook");
    }

    private void doSomethingWhenSuccess() {
        Log.d("RxLog", "doSomethingWhenSuccess");
    }

    private Container createContainer(boolean isSuccess) {
        List<Book> books = new ArrayList<>();
        books.add(new Book("good book1"));
        books.add(new Book("good book2"));
        books.add(new Book("good book3"));

        return new Container(isSuccess, books);
    }

    static class Book {
        public String name;

        public Book(String name) {

            this.name = name;
        }
    }

    static class Container {
        boolean success;
        List<Book> books;

        public Container(boolean success, List<Book> books) {
            this.success = success;
            this.books = books;
        }
    }


    private static class ContainerCheck implements Observable.Operator<Container, Container> {
        @Override
        public Subscriber<? super Container> call(Subscriber<? super Container> o) {
            return new Subscriber<Container>() {
                @Override
                public void onCompleted() {
                    if (o.isUnsubscribed()) return;
                    o.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    if (o.isUnsubscribed()) return;
                    o.onError(e);
                }

                @Override
                public void onNext(Container container) {
                    if (o.isUnsubscribed()) return;
                    if (container.success) {
                        o.onNext(container);
                    } else {
                        o.onError(new Throwable("your costom exception"));
                    }
                }
            };
        }
    }
}
