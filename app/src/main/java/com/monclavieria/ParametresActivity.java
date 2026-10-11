package com.monclavieria;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.content.SharedPreferences;

public class ParametresActivity extends Activity {

    private SharedPreferences preferences;
    private LinearLayout page;
    private TextView titreHauteur;
    private int hauteur = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("clavier_preferences", MODE_PRIVATE);
        hauteur = preferences.getInt("keyboard_height", 44);

        construirePage();
    }

    private void construirePage() {
        String theme = preferences.getString("theme", "clair");
        boolean sombre = theme.equals("sombre");

        int fond = sombre ? Color.rgb(30, 32, 38) : Color.rgb(245, 247, 251);
        int texte = sombre ? Color.WHITE : Color.rgb(30, 35, 45);

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        page.setBackgroundColor(fond);

        TextView titre = new TextView(this);
        titre.setText("Paramètres du clavier");
        titre.setTextSize(24);
        titre.setTextColor(texte);
        titre.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(titre);

        TextView sousTitre = new TextView(this);
        sousTitre.setText("Personnalise ton expérience MonClavierIA");
        sousTitre.setTextSize(14);
        sousTitre.setTextColor(texte);
        sousTitre.setPadding(0, dp(8), 0, dp(28));
        page.addView(sousTitre);

        ajouterTitre("Apparence", texte);

        Button clair = creerBouton("Thème clair", texte, fond);
        clair.setOnClickListener(v -> {
            preferences.edit().putString("theme", "clair").apply();
            construirePage();
        });
        page.addView(clair);

        Button sombreBouton = creerBouton("Thème sombre", texte, fond);
        sombreBouton.setOnClickListener(v -> {
            preferences.edit().putString("theme", "sombre").apply();
            construirePage();
        });
        page.addView(sombreBouton);

        TextView etatTheme = new TextView(this);
        etatTheme.setText("Thème actuel : " + (sombre ? "sombre" : "clair"));
        etatTheme.setTextColor(texte);
        etatTheme.setPadding(0, dp(8), 0, dp(22));
        page.addView(etatTheme);

        ajouterTitre("Retour tactile", texte);

        Switch vibration = new Switch(this);
        vibration.setText("Vibration des touches");
        vibration.setTextColor(texte);
        vibration.setTextSize(16);
        vibration.setChecked(preferences.getBoolean("vibration", true));
        vibration.setPadding(0, dp(10), 0, dp(24));
        vibration.setOnCheckedChangeListener((bouton, active) ->
            preferences.edit().putBoolean("vibration", active).apply()
        );
        page.addView(vibration);

        ajouterTitre("Taille du clavier", texte);

        titreHauteur = new TextView(this);
        titreHauteur.setText("Hauteur des touches : " + hauteur);
        titreHauteur.setTextColor(texte);
        titreHauteur.setPadding(0, dp(8), 0, dp(8));
        page.addView(titreHauteur);

        SeekBar curseur = new SeekBar(this);
        curseur.setMax(20);
        curseur.setProgress(hauteur - 36);
        curseur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar barre, int progression, boolean utilisateur) {
                hauteur = progression + 36;
                titreHauteur.setText("Hauteur des touches : " + hauteur);
            }

            @Override
            public void onStartTrackingTouch(SeekBar barre) {}

            @Override
            public void onStopTrackingTouch(SeekBar barre) {
                preferences.edit().putInt("keyboard_height", hauteur).apply();
            }
        });
        page.addView(curseur);

        TextView explication = new TextView(this);
        explication.setText("La hauteur sera appliquée au clavier lors de son prochain affichage.");
        explication.setTextColor(texte);
        explication.setTextSize(13);
        explication.setPadding(0, dp(8), 0, dp(24));
        page.addView(explication);

        Button retour = creerBouton("Terminer", texte, fond);
        retour.setOnClickListener(v -> finish());
        page.addView(retour);

        setContentView(page);
    }

    private void ajouterTitre(String texte, int couleur) {
        TextView vue = new TextView(this);
        vue.setText(texte);
        vue.setTextColor(couleur);
        vue.setTextSize(18);
        vue.setTypeface(null, android.graphics.Typeface.BOLD);
        vue.setPadding(0, dp(10), 0, dp(8));
        page.addView(vue);
    }

    private Button creerBouton(String texte, int couleur, int fond) {
        Button bouton = new Button(this);
        bouton.setText(texte);
        bouton.setTextColor(couleur);
        bouton.setAllCaps(false);
        bouton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                fond == Color.WHITE ? Color.rgb(225, 230, 238) : fond
            )
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            -1, dp(48)
        );
        params.setMargins(0, dp(3), 0, dp(3));
        bouton.setLayoutParams(params);
        return bouton;
    }

    private int dp(int valeur) {
        return (int) (valeur * getResources().getDisplayMetrics().density + 0.5f);
    }
}
