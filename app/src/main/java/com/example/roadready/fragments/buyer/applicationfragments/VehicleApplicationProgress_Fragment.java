package com.example.roadready.fragments.buyer.applicationfragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.roadready.R;
import com.example.roadready.classes.general.MainFacade;
import com.example.roadready.classes.general.RoadReadyServer;
import com.example.roadready.classes.model.gson.ApplicationsDataGson;
import com.example.roadready.classes.model.gson.ListingsDataGson;
import com.example.roadready.classes.model.gson.data.ApplicationGson;
import com.example.roadready.classes.model.gson.data.VehicleGson;
import com.example.roadready.classes.model.gson.response.SuccessGson;
import com.example.roadready.databinding.FragmentBuyerVehicleApplicationProgressBinding;
import com.squareup.picasso.Picasso;

public class VehicleApplicationProgress_Fragment extends Fragment {

    private FragmentBuyerVehicleApplicationProgressBinding binding;
    private MainFacade mainFacade;
    private String modelId;
    private ApplicationGson application;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBuyerVehicleApplicationProgressBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
        binding.apContenContainer.setVisibility(View.INVISIBLE);

        modelId = VehicleApplicationProgress_FragmentArgs.fromBundle(getArguments()).getModelId();

        final RoadReadyServer.ResponseListener<ApplicationsDataGson> responseListener = new RoadReadyServer.ResponseListener<ApplicationsDataGson>() {
            @Override
            public void onSuccess(SuccessGson<ApplicationsDataGson> response) {
                for(ApplicationGson applicationGson : response.getData().getApplications()) {
                    if(applicationGson.getId().equals(modelId)) {
                        application = applicationGson;
                        getVehicleInfo(applicationGson);
                        break;
                    }
                }

                if (binding != null)
                    binding.apContenContainer.setVisibility(View.VISIBLE);
                mainFacade.hideProgressBar();
            }

            @Override
            public void onFailure(int code, String message) {
                if (binding != null)
                    binding.apContenContainer.setVisibility(View.VISIBLE);
                mainFacade.makeToast(message, Toast.LENGTH_SHORT);
                mainFacade.hideProgressBar();
            }
        };

        mainFacade.getBuyerApplications(responseListener);
    }

    private void getVehicleInfo(ApplicationGson applicationGson){
        getApplicationForm(applicationGson.getApplicationType());
        final RoadReadyServer.ResponseListener<ListingsDataGson> responseListener = new RoadReadyServer.ResponseListener<ListingsDataGson>() {
            @Override
            public void onSuccess(SuccessGson<ListingsDataGson> response) {
                updateVehicleInfo(response.getData().getListing());
            }

            @Override
            public void onFailure(int code, String message) {
                if (code != -1)
                    mainFacade.makeToast(message, Toast.LENGTH_SHORT);
            }
        };

        mainFacade.getListings(responseListener, applicationGson.getListingId(), null, null);
    }

    private void getApplicationForm(String applicationType){
        switch(applicationType){
            case "cash":
                openCashForm();
                break;
            case "inhouseFinance":
                openInhouseForm();
                break;
            case "bankLoan(dealershipBankChoice)":
                openBankLoanDealership();
                break;
            case "bankLoan(buyerBankChoice)":
                openBankLoanBuyer();
        }
    }

    private void updateVehicleInfo(VehicleGson vehicleGson) {
        if (binding != null) {
            binding.apLblItem.setText(vehicleGson.getModelAndName());
            Picasso.get().load(vehicleGson.getImageUrl()).into(binding.apImageItem);
        }
    }

    private void openCashForm(){
        binding.apContainerCash.setVisibility(View.VISIBLE);

        setProcess(binding.apContainerCash, application.getProgress(), application.getStatus());
    }

    private void openInhouseForm(){
        binding.apContainerInhouse.setVisibility(View.VISIBLE);

        setProcess(binding.apContainerInhouse, application.getProgress(), application.getStatus());
    }

    private void openBankLoanDealership(){
        binding.apContainerDealershipChoice.setVisibility(View.VISIBLE);

        setProcess(binding.apContainerDealershipChoice, application.getProgress(), application.getStatus());
    }

    private void openBankLoanBuyer(){
        binding.apContainerBuyerChoice.setVisibility(View.VISIBLE);

        setProcess(binding.apContainerBuyerChoice, application.getProgress(), application.getStatus());
    }

    private void setProcess(LinearLayout parentContainer, int progress, String status){
        for (int i = 0; i < parentContainer.getChildCount(); i++) {
            LinearLayout child = (LinearLayout) parentContainer.getChildAt(i);
            if (progress > i) setProcessSuccessful(child);
            if(status.equals("rejected")){
                if (progress <= i) setProcessRejected(child);
            }else {
                if (progress == i) setProcessPendingAnswerable(child);
                if (progress < i) setProcessPending(child);
            }
        }
    }

    private void setProcessPending(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout rl = (RelativeLayout) child;
                rl.removeAllViews();
                ProgressBar pb = new ProgressBar(mainFacade.getMainActivity().getApplicationContext());
                pb.setIndeterminateTintList(ColorStateList.valueOf(Color.rgb(248, 31, 20)));
                rl.addView(pb);
            }
            container.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(248, 31, 20)));
        }
    }

    private void setProcessPendingAnswerable(LinearLayout container){
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout rl = (RelativeLayout) child;
                rl.removeAllViews();
                ProgressBar pb = new ProgressBar(mainFacade.getMainActivity().getApplicationContext());
                pb.setIndeterminateTintList(ColorStateList.valueOf(Color.rgb(248, 31, 20)));
                rl.addView(pb);
            }
            if (child instanceof Button) {
                child.setVisibility(View.VISIBLE);
            }
            container.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(248, 31, 20)));
        }
    }

    private void setProcessSuccessful(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout rl = (RelativeLayout) child;
                rl.removeAllViews();
                ImageView iv = new ImageView(mainFacade.getMainActivity().getApplicationContext());
                iv.setImageResource(R.drawable.check_mark);
                iv.setColorFilter(Color.rgb(32,244,14));
                rl.addView(iv);
            }
            container.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(32, 244, 14)));
        }
    }

    private void setProcessRejected(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof RelativeLayout) {
                RelativeLayout rl = (RelativeLayout) child;
                rl.removeAllViews();
                ImageView iv = new ImageView(mainFacade.getMainActivity().getApplicationContext());
                iv.setImageResource(R.drawable.mv_iv_removevehicle);
                iv.setColorFilter(Color.rgb(248,32,14));
                rl.addView(iv);
            }
            container.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(248, 32, 14)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}