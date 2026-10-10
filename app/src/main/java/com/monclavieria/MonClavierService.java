package com.monclavieria;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

public class MonClavierService extends InputMethodService {
    @Override
    public View onCreateInputView() {
        LinearLayout clavier = new LinearLayout(this);
        clavier.setOrientation(LinearLayout.VERTICAL);
        clavier.setPadding(6, 6, 6, 6);
        String[][] lignes = {
            {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"},
            {"a", "s", "d", "f", "g", "h", "j", "k", "l"},
            {"z", "x", "c", "v", "b", "n", "m"}
        };
        for (String[] ligne : lignes) {
            LinearLayout rang = new LinearLayout(this);
            rang.setOrientation(LinearLayout.HORIZONTAL);
            for (String lettre : ligne) {
                Button touche = new Button(this);
                touche.setText(lettre);
                touche.setAllCaps(false);
                touche.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                params.setMargins(2, 2, 2, 2);
                rang.addView(touche, params);
                touche.setOnClickListener(v -> {
                    InputConnection connexion = getCurrentInputConnection();
                    if (connexion != null) connexion.commitText(lettre, 1);
                });
            }
            clavier.addView(rang);
        }
        LinearLayout rangBas = new LinearLayout(this);
        rangBas.setOrientation(LinearLayout.HORIZONTAL);
        Button espace = new Button(this);
        espace.setText("Espace");
        espace.setAllCaps(false);
        espace.setOnClickListener(v -> {
            InputConnection connexion = getCurrentInputConnection();
            if (connexion != null) connexion.commitText(" ", 1);
        });
        rangBas.addView(espace, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
        Button retour = new Button(this);
        retour.setText("⌫");
        retour.setOnClickListener(v -> {
            InputConnection connexion = getCurrentInputConnection();
            if (connexion != null) connexion.deleteSurroundingText(1, 0);
        });
        rangBas.addView(retour, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        clavier.addView(rangBas);
        return clavier;
    }
}
