package com.example.roadready.fragments.buyer.myvehiclefragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.roadready.classes.general.MainFacade;
import com.example.roadready.classes.general.RoadReadyServer;
import com.example.roadready.classes.model.gson.ApplicationsDataGson;
import com.example.roadready.classes.model.gson.data.ApplicationGson;
import com.example.roadready.classes.model.gson.response.SuccessGson;
import com.example.roadready.classes.ui.adapter.BuyerApplicationLayoutAdapter;

import java.util.List;

public class DocumentsSubmitted_Fragment extends Fragment {
    private final String TAG = "DocumentsSubmitted_Fragment";
    private com.example.roadready.databinding.FragmentBuyerDocumentsSubmittedBinding binding;
    private MainFacade mainFacade;

    private ApplicationGson applicationGson;
    private int applicationCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = com.example.roadready.databinding.FragmentBuyerDocumentsSubmittedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        applicationCount = 0;

        try {
            mainFacade = MainFacade.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mainFacade.hideProgressBar();
        mainFacade.hideBackDrop();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainFacade.showProgressBar();

        final RoadReadyServer.ResponseListener<ApplicationsDataGson> responseListener = new RoadReadyServer.ResponseListener<ApplicationsDataGson>() {
            @Override
            public void onSuccess(SuccessGson<ApplicationsDataGson> response) {
                List<ApplicationGson> applications = response.getData().getApplications();
                if (binding != null) {
                    ViewPager2 viewPager = binding.apViewPagerContainer;
                    viewPager.setAdapter(new BuyerApplicationLayoutAdapter(requireActivity(), applications));
                }
                applicationCount = applications.size();
                setApplicationCount();
                mainFacade.hideProgressBar();
            }

            @Override
            public void onFailure(int code, String message) {
                if (code != -1)
                    mainFacade.makeToast(message, Toast.LENGTH_SHORT);
                setApplicationCount();
                mainFacade.hideProgressBar();
            }
        };

        mainFacade.getBuyerApplications(responseListener);
        initActions();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainFacade.hideProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private void initActions() {

    }

    public void setApplicationCount(){
        if (binding != null) {
            if(applicationCount <= 0){
                binding.apTxtNoDocuments.setVisibility(View.VISIBLE);
            }
        }
    }
}