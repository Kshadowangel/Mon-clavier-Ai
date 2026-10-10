package com.monclavieria;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonClavierService extends InputMethodService {
    private boolean chiffres = false;
    private boolean majuscules = false;
    private LinearLayout clavier;
    private final List<Button> touchesLettres = new ArrayList<>();
    private Button toucheMaj;
    private Button toucheMode;

    @Override
    public View onCreateInputView() {
        return creerClavier();
    }

    private View creerClavier() {
        touchesLettres.clear();
        clavier = new LinearLayout(this);
        clavier.setOrientation(LinearLayout.VERTICAL);
        clavier.setPadding(dp(3), dp(3), dp(3), dp(3));
        clavier.setBackgroundColor(Color.rgb(225, 230, 238));

        String[][] lettres = {
            {"q","w","e","r","t","y","u","i","o","p"},
            {"a","s","d","f","g","h","j","k","l"},
            {"⇧","z","x","c","v","b","n","m","⌫"}
        };
        String[][] symboles = {
            {"1","2","3","4","5","6","7","8","9","0"},
            {"@","#","$","%","&","-","+","(",")"},
            {"=","/",":",";","!","?","'","\"","⌫"}
        };
        String[][] lignes = chiffres ? symboles : lettres;

        for (String[] ligne : lignes) {
            LinearLayout rang = new LinearLayout(this);
            rang.setOrientation(LinearLayout.HORIZONTAL);
            rang.setGravity(Gravity.CENTER);
            for (String symbole : ligne) {
                String affiche = symbole;
                if (!chiffres && majuscules && symbole.length() == 1
                        && Character.isLetter(symbole.charAt(0))) {
                    affiche = symbole.toUpperCase(Locale.ROOT);
                }
                Button touche = creerTouche(affiche);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, dp(44), 1f);
                params.setMargins(dp(2), dp(2), dp(2), dp(2));
                rang.addView(touche, params);
                if (!chiffres && symbole.length() == 1
                        && Character.isLetter(symbole.charAt(0))) {
                    touchesLettres.add(touche);
                }
                if (symbole.equals("⇧")) toucheMaj = touche;
                touche.setOnClickListener(v -> traiterTouche(symbole));
            }
            clavier.addView(rang);
        }

        LinearLayout bas = new LinearLayout(this);
        bas.setOrientation(LinearLayout.HORIZONTAL);
        bas.setGravity(Gravity.CENTER);

        toucheMode = creerTouche(chiffres ? "ABC" : "?123");
        ajouterTouche(bas, toucheMode, 1.5f);
        toucheMode.setOnClickListener(v -> {
            chiffres = !chiffres;
            majuscules = false;
            setInputView(creerClavier());
        });

        Button virgule = creerTouche(",");
        ajouterTouche(bas, virgule, 1f);
        virgule.setOnClickListener(v -> envoyer(","));

        Button espace = creerTouche("Espace");
        ajouterTouche(bas, espace, 4f);
        espace.setOnClickListener(v -> envoyer(" "));

        Button point = creerTouche(".");
        ajouterTouche(bas, point, 1f);
        point.setOnClickListener(v -> envoyer("."));

        Button entree = creerTouche("↵");
        ajouterTouche(bas, entree, 1.5f);
        entree.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            EditorInfo info = getCurrentInputEditorInfo();
            if (info != null && info.actionId != EditorInfo.IME_ACTION_NONE
                    && info.actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(info.actionId);
            } else {
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
            }
        });
        Button quiz = creerTouche("QUIZ");
        ajouterTouche(bas, quiz, 1.5f);
        quiz.setOnClickListener(v -> chargerQuestionDepuisPressePapiers());

        clavier.addView(bas);
        return clavier;
    }

    private void traiterTouche(String touche) {
        if (touche.equals("⇧")) {
            majuscules = !majuscules;
            actualiserMajuscules();
        } else if (touche.equals("⌫")) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.deleteSurroundingText(1, 0);
        } else {
            String caractere = touche;
            if (!chiffres && majuscules && caractere.length() == 1
                    && Character.isLetter(caractere.charAt(0))) {
                caractere = caractere.toUpperCase(Locale.ROOT);
                majuscules = false;
                actualiserMajuscules();
            }
            envoyer(caractere);
        }
    }

    private void actualiserMajuscules() {
        if (toucheMaj != null) {
            toucheMaj.setText(majuscules ? "⇧✓" : "⇧");
        }
        for (Button touche : touchesLettres) {
            String lettre = touche.getText().toString();
            touche.setText(majuscules ? lettre.toUpperCase(Locale.ROOT)
                                      : lettre.toLowerCase(Locale.ROOT));
        }
    }

    private void chargerQuestionDepuisPressePapiers() {
      ClipboardManager pressePapiers =
          (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

      if (pressePapiers == null || !pressePapiers.hasPrimaryClip()) {
          Toast.makeText(this, "Copie d'abord la question.",
              Toast.LENGTH_LONG).show();
          return;
      }

      ClipData clip = pressePapiers.getPrimaryClip();
      if (clip == null || clip.getItemCount() == 0) {
          Toast.makeText(this, "Presse-papiers vide.",
              Toast.LENGTH_LONG).show();
          return;
      }

      CharSequence texte = clip.getItemAt(0).coerceToText(this);
      if (texte == null || texte.toString().trim().isEmpty()) {
          Toast.makeText(this, "Aucune question trouvée.",
              Toast.LENGTH_LONG).show();
          return;
      }

      Toast.makeText(this,
          "Question récupérée : " + texte.toString().trim(),
          Toast.LENGTH_LONG).show();
  }

    private void envoyer(String texte) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(texte, 1);
    }

    private Button creerTouche(String texte) {
        Button b = new Button(this);
        b.setText(texte);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        b.setTextColor(Color.rgb(30, 35, 45));
        b.setPadding(0, 0, 0, 0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setBackground(fondTouche());
        b.setGravity(Gravity.CENTER);
        return b;
    }

    private GradientDrawable fondTouche() {
        GradientDrawable fond = new GradientDrawable();
        fond.setColor(Color.WHITE);
        fond.setCornerRadius(dp(7));
        fond.setStroke(dp(1), Color.rgb(205, 212, 222));
        return fond;
    }

    private void ajouterTouche(LinearLayout rang, Button touche, float poids) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, dp(44), poids);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        rang.addView(touche, params);
    }

    private int dp(int valeur) {
        return (int) (valeur * getResources().getDisplayMetrics().density + 0.5f);
    }
}
