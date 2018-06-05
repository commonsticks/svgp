package com.keldee.svgp4.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keldee.svgp4.R;

public class SpeedListDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_ITEM_COUNT = "item_count";
    private Listener callback;

    public static SpeedListDialogFragment newInstance(int itemCount) {
        final SpeedListDialogFragment fragment = new SpeedListDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_ITEM_COUNT, itemCount);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCallback (Listener callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SpeedAdapter(getArguments().getInt(ARG_ITEM_COUNT)));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        callback = null;
        super.onDetach();
    }

    public interface Listener {
        void onSpeedClicked(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_speed_list_dialog_item, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onSpeedClicked(getAdapterPosition());
                        dismiss();
                    }
                }
            });
        }

    }

    private class SpeedAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;
        private int curItem = 0;

        SpeedAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (curItem) {
                case 0:
                    holder.text.setText(R.string.EDITOR_LIST_DIALOG_ITEM_0);
                    break;
                case 1:
                    holder.text.setText(R.string.EDITOR_LIST_DIALOG_ITEM_1);
                    break;
                case 2:
                    holder.text.setText(R.string.EDITOR_LIST_DIALOG_ITEM_2);
                    break;
                case 3:
                    holder.text.setText(R.string.EDITOR_LIST_DIALOG_ITEM_3);
                    break;
                default:
                    return;
            }
            curItem++;
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }

}
