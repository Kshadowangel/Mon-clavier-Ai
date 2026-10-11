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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;

public class MonClavierService extends InputMethodService {
    private boolean chiffres = false;
    private boolean majuscules = false;
    private LinearLayout clavier;
    private final List<Button> touchesLettres = new ArrayList<>();
    private Button toucheMaj;
    private Button toucheMode;
    private LinearLayout barreSuggestions;

    private final String[] dictionnaireSuggestions = {
        "bonjour", "bonsoir", "bonne", "bon", "beaucoup", "besoin",
        "bien", "bientôt", "bienvenue", "comment", "comme", "comprendre",
        "compte", "coucou", "cette", "cela", "ceci", "c'est",
        "dans", "demain", "depuis", "dernier", "des", "deux",
        "dire", "donc", "d'accord", "encore", "enfin", "ensemble",
        "est", "être", "et", "excuse", "exactement", "faire",
        "fait", "famille", "femme", "félicitations", "fois", "français",
        "grand", "grande", "groupe", "génial", "gens", "jour",
        "journée", "jamais", "je", "j'aime", "j'espère", "j'arrive",
        "maintenant", "mais", "merci", "meilleur", "même", "message",
        "mon", "monde", "mot", "moi", "non", "notre",
        "nouveau", "nouvelle", "nous", "oui", "parce", "parfait",
        "parler", "partie", "personne", "petit", "petite", "peut",
        "plus", "pour", "pourquoi", "pourrais", "premier", "projet",
        "quel", "quelle", "question", "qui", "quoi", "rapidement",
        "rien", "salut", "savoir", "seulement", "si", "soir",
        "super", "suis", "sur", "surtout", "temps", "toujours",
        "tout", "très", "travail", "trouver", "tu", "vraiment",
        "venir", "votre", "vous", "vouloir", "voyage", "ça"
    };

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

        barreSuggestions = new LinearLayout(this);
        barreSuggestions.setOrientation(LinearLayout.HORIZONTAL);
        barreSuggestions.setGravity(Gravity.CENTER);
        barreSuggestions.setVisibility(View.GONE);
        clavier.addView(barreSuggestions,
            new LinearLayout.LayoutParams(-1, dp(36)));

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
                if (symbole.equals("⌫")) {
                    touche.setOnTouchListener((vue, evenement) -> {
                        int action = evenement.getAction();

                        if (action == android.view.MotionEvent.ACTION_DOWN) {
                            vue.performHapticFeedback(
                                android.view.HapticFeedbackConstants.KEYBOARD_TAP
                            );
                            traiterTouche("⌫");

                            Handler gestionnaire =
                                new Handler(Looper.getMainLooper());
                            boolean[] appuye = {true};

                            Runnable repetition = new Runnable() {
                                @Override
                                public void run() {
                                    if (appuye[0]) {
                                        traiterTouche("⌫");
                                        gestionnaire.postDelayed(this, 70);
                                    }
                                }
                            };

                            vue.setTag(new Object[] {
                                gestionnaire, repetition, appuye
                            });
                            gestionnaire.postDelayed(repetition, 350);
                            return true;
                        }

                        if (action == android.view.MotionEvent.ACTION_UP
                                || action == android.view.MotionEvent.ACTION_CANCEL) {
                            Object objet = vue.getTag();
                            if (objet instanceof Object[]) {
                                Object[] donnees = (Object[]) objet;
                                ((boolean[]) donnees[2])[0] = false;
                                ((Handler) donnees[0]).removeCallbacks(
                                    (Runnable) donnees[1]
                                );
                            }
                            vue.setTag(null);
                            return true;
                        }

                        return true;
                    });
                } else {
                    touche.setOnClickListener(v -> traiterTouche(symbole));
                }

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
        quiz.setOnClickListener(v -> {
            Toast.makeText(this, "Clic QUIZ détecté !", Toast.LENGTH_SHORT).show();
            chargerQuestionDepuisPressePapiers();
        });

        clavier.addView(bas);
        return clavier;
    }

    private void traiterTouche(String touche) {
        if (touche.equals("⇧")) {
            majuscules = !majuscules;
            actualiserMajuscules();
        } else if (touche.equals("⌫")) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.deleteSurroundingText(1, 0);
                actualiserSuggestions();
            }
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

    private void actualiserSuggestions() {
        if (barreSuggestions == null) return;

        barreSuggestions.removeAllViews();

        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            barreSuggestions.setVisibility(View.GONE);
            return;
        }

        CharSequence texte = ic.getTextBeforeCursor(80, 0);
        if (texte == null || texte.length() == 0) {
            barreSuggestions.setVisibility(View.GONE);
            return;
        }

        String avant = texte.toString();
        int debut = avant.length();

        while (debut > 0) {
            char c = avant.charAt(debut - 1);
            if (Character.isLetter(c)) {
                debut--;
            } else {
                break;
            }
        }

        String prefixe = avant.substring(debut).toLowerCase(Locale.ROOT);
        if (prefixe.length() < 2) {
            barreSuggestions.setVisibility(View.GONE);
            return;
        }

        List<String> propositions = new ArrayList<>();
        for (String mot : dictionnaireSuggestions) {
            if (mot.toLowerCase(Locale.ROOT).startsWith(prefixe)
                    && !mot.equalsIgnoreCase(prefixe)
                    && !propositions.contains(mot)) {
                propositions.add(mot);
                if (propositions.size() == 3) break;
            }
        }

        if (propositions.isEmpty()) {
            barreSuggestions.setVisibility(View.GONE);
            return;
        }

        barreSuggestions.setVisibility(View.VISIBLE);

        for (String mot : propositions) {
            Button proposition = creerTouche(mot);
            proposition.setTextSize(13);
            LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, dp(32), 1f);
            params.setMargins(dp(2), 0, dp(2), 0);
            barreSuggestions.addView(proposition, params);

            proposition.setOnClickListener(v -> {
                InputConnection connexion = getCurrentInputConnection();
                if (connexion == null) return;

                CharSequence contenu = connexion.getTextBeforeCursor(80, 0);
                if (contenu == null) return;

                String courant = contenu.toString();
                int position = courant.length();

                while (position > 0
                        && Character.isLetter(courant.charAt(position - 1))) {
                    position--;
                }

                if (position < courant.length()) {
                    connexion.deleteSurroundingText(
                        courant.length() - position, 0);
                    connexion.commitText(mot + " ", 1);
                }

                actualiserSuggestions();
            });
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

        String question = texte.toString().trim();
        Toast.makeText(this, "Gemini prépare la réponse...",
            Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            HttpURLConnection connexion = null;
            try {
                URL url = new URL("http://127.0.0.1:5000/ask");
                connexion = (HttpURLConnection) url.openConnection();
                connexion.setRequestMethod("POST");
                connexion.setConnectTimeout(10000);
                connexion.setReadTimeout(30000);
                connexion.setDoOutput(true);
                connexion.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8");

                JSONObject demande = new JSONObject();
                demande.put("message",
                    "Tu es un assistant intelligent spécialisé dans les questions de quiz, " +
"les connaissances générales, les définitions et les explications. " +
"Adapte la longueur de ta réponse à la question posée. " +
"Pour une question simple, donne une réponse courte, exacte et directe. " +
"Pour une définition, donne une définition claire, complète et facile à comprendre. " +
"Pour une demande d'explication, développe suffisamment la réponse, " +
"en présentant les idées dans un ordre logique et en ajoutant des exemples utiles si nécessaire. " +
"Pour une question complexe, structure ta réponse pour qu'elle soit facile à comprendre. " +
"Respecte précisément ce que la question demande. " +
"N'invente jamais de faits, de noms, de dates ou de références. " +
"Si tu ne connais pas la réponse ou si tu as un doute important, " +
"signale-le honnêtement. " +
"Évite les introductions inutiles et les répétitions. " +
"Question : " + question
                    );

                byte[] donnees =
                    demande.toString().getBytes(StandardCharsets.UTF_8);

                try (OutputStream sortie = connexion.getOutputStream()) {
                    sortie.write(donnees);
                }

                int statut = connexion.getResponseCode();
                InputStream flux = (statut >= 200 && statut < 300)
                    ? connexion.getInputStream()
                    : connexion.getErrorStream();

                if (flux == null) {
                    throw new IOException("Réponse HTTP " + statut);
                }

                StringBuilder contenu = new StringBuilder();
                try (BufferedReader lecteur = new BufferedReader(
                        new InputStreamReader(flux, StandardCharsets.UTF_8))) {
                    String ligne;
                    while ((ligne = lecteur.readLine()) != null) {
                        contenu.append(ligne);
                    }
                }

                JSONObject resultat = new JSONObject(contenu.toString());
                String reponse = nettoyerReponse(resultat.optString("reponse", ""));

                if (statut < 200 || statut >= 300 || reponse.isEmpty()) {
                    throw new IOException(resultat.optString(
                        "erreur", "Erreur de l'API Gemini."));
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) {
                        ClipboardManager gestionPressePapiers =
                            (ClipboardManager) getSystemService(
                                Context.CLIPBOARD_SERVICE);
                        if (gestionPressePapiers != null) {
                            gestionPressePapiers.setPrimaryClip(
                                ClipData.newPlainText("Réponse", reponse));
                        }
                        ic.commitText(reponse, 1);
                        Toast.makeText(this, "Réponse insérée et copiée !",
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Champ de texte indisponible.",
                            Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                String detail = e.getMessage();
                if (detail == null || detail.isEmpty()) {
                    detail = "Impossible de contacter l'API.";
                }
                final String erreur = detail;
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(this, "Échec : " + erreur,
                        Toast.LENGTH_LONG).show());
            } finally {
                if (connexion != null) connexion.disconnect();
            }
        }).start();
    }

    private String nettoyerReponse(String texte) {
        if (texte == null) return "";
        return texte
            .replace("**", "")
            .replace("__", "")
            .replace("`", "")
            .replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "")
            .trim();
    }

    private void envoyer(String texte) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(texte, 1);
            actualiserSuggestions();
        }
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

        // Retour tactile et visuel au contact du doigt.
        b.setOnTouchListener((vue, evenement) -> {
            int action = evenement.getAction();

            if (action == android.view.MotionEvent.ACTION_DOWN) {
                vue.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP
                );
                vue.setAlpha(0.72f);
            } else if (action == android.view.MotionEvent.ACTION_UP
                    || action == android.view.MotionEvent.ACTION_CANCEL) {
                vue.setAlpha(1.0f);
            }

            return false;
        });

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
