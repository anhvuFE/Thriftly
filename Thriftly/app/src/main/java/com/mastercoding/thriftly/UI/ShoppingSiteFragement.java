package com.mastercoding.thriftly.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mastercoding.thriftly.Adapter.CategoryAdapter;
import com.mastercoding.thriftly.Adapter.ProductAdapter;
import com.mastercoding.thriftly.Adapter.ProductShoppingSiteAdapter;
import com.mastercoding.thriftly.Authen.SignInActivity;
import com.mastercoding.thriftly.Models.Category;
import com.mastercoding.thriftly.Models.Product;
import com.mastercoding.thriftly.R;

import java.util.ArrayList;
import java.util.List;

public class ShoppingSiteFragement extends Fragment {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private RecyclerView categoryRecycler;
    private ProductShoppingSiteAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList;
    private List<Category> categoryList;
    private FirebaseFirestore db;
    private TextView emptyPost;

    private void bindingView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_products);
        categoryRecycler = view.findViewById(R.id.category_recycler_view);
        emptyPost = view.findViewById(R.id.tvNotFound);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categoryRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_site, container, false);

        bindingView(view);
        checkUserLoginAndEmailVerification();

        // Lấy userId của người dùng hiện tại
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
        }
        // Khởi tạo adapter một lần
        productList = new ArrayList<>();
        productAdapter = new ProductShoppingSiteAdapter(productList, currentUserId);
        recyclerView.setAdapter(productAdapter);

        loadProducts();
        loadCategory();


        return view;
    }

    private void checkUserLoginAndEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            if (!user.isEmailVerified()) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
                getActivity().finish();
                Toast.makeText(getActivity(), "Please verify your email before proceeding", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Tải danh sách sản phẩm từ Firestore
    private void loadProducts() {

        db.collection("Products").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tạo một Product từ documentSnapshot
                        for (QueryDocumentSnapshot document : task.getResult()){
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }

                        if (productList.isEmpty()) {
                            emptyPost.setVisibility(View.VISIBLE);
                        }else{
                            emptyPost.setVisibility(View.GONE);
                        }

                    productAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadCategory(){

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            loadProductsByCategory(category.getId());
        });

        categoryRecycler.setAdapter(categoryAdapter);
        db.collection("Categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Category category = document.toObject(Category.class);
                            category.setId(document.getId());
                            categoryList.add(category);
                        }

                        if (productList.isEmpty()) {
                            emptyPost.setVisibility(View.VISIBLE);
                        }else{
                            emptyPost.setVisibility(View.GONE);
                        }

                        categoryAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadProductsByCategory(String categoryId) {
        productList.clear();

        db.collection("Products")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Create a Product object from the Firestore document
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }

                        if (productList.isEmpty()) {
                            emptyPost.setVisibility(View.VISIBLE);
                        }else{
                            emptyPost.setVisibility(View.GONE);
                        }

                        productAdapter.notifyDataSetChanged();
                    } else {

                        Toast.makeText(getContext(), "Failed to load products.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
