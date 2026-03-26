package com.dorin.inchirierimasini.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.dorin.inchirierimasini.R;

public class ConfirmDeleteDialog extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ITEM_ID = "item_id";

    private OnDeleteConfirmedListener listener;

    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed(long itemId);
        void onDeleteCancelled();
    }

    public static ConfirmDeleteDialog newInstance(String title, String message, long itemId) {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putLong(ARG_ITEM_ID, itemId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDeleteConfirmedListener) {
            listener = (OnDeleteConfirmedListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnDeleteConfirmedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE, getString(R.string.confirm_delete)) : getString(R.string.confirm_delete);
        String message = getArguments() != null ? getArguments().getString(ARG_MESSAGE, getString(R.string.delete_confirmation_message)) : getString(R.string.delete_confirmation_message);
        long itemId = getArguments() != null ? getArguments().getLong(ARG_ITEM_ID, -1) : -1;

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (listener != null) {
                        listener.onDeleteConfirmed(itemId);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (listener != null) {
                        listener.onDeleteCancelled();
                    }
                })
                .create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
