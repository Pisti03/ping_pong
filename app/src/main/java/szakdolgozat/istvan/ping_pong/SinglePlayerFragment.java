package szakdolgozat.istvan.ping_pong;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SinglePlayerFragment extends Fragment implements View.OnClickListener{

    private Button start;
    private Spinner spinner;

    public static SinglePlayerFragment newInstance() {
        return new SinglePlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_player, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        start = (Button) getView().findViewById(R.id.buttonStart);
        start.setOnClickListener(this);

        spinner = (Spinner) getView().findViewById(R.id.spinnerDifficulties);
        List<String> list = new ArrayList<String>();
        list.add("Easy");
        list.add("Medium");
        list.add("Hard");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, list);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                System.out.println("buttonStart pressed");
                Intent intent = new Intent(getActivity(), GameActivity.class);
                Bundle b = new Bundle();
                b.putInt("players", 1); //Your id
                int temp;
                switch (String.valueOf(spinner.getSelectedItem())){
                    case "Easy":
                        temp=1;
                        break;
                    case "Medium":
                        temp=2;
                        break;
                    case "Hard":
                        temp=3;
                        break;
                    default:
                        temp=2;
                        break;
                }
                b.putInt("difficulty", temp);
                intent.putExtras(b);
                startActivity(intent);
            break;
        }
    }

}
