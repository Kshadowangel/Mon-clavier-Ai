package com.monclavieria;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
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
import android.widget.PopupMenu;
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
    private long generationRequeteIA = 0L;
    private long dernierHorodatageEnvoi = 0L;
    private String derniereQuestionEnvoyee = "";
    private String dernierModeEnvoye = "";


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
        "venir", "votre", "vous", "vouloir", "voyage", "ça",
        "absolument", "accepter", "accord", "accueil", "action",
        "activité", "actualité", "addition", "admirable", "adore",
        "affaire", "afficher", "aider", "aimable", "aimer",
        "ainsi", "ajouter", "améliorer", "amitié", "amour",
        "analyse", "ancien", "année", "annoncer", "apprendre",
        "après", "arriver", "article", "assez", "attention",
        "aujourd'hui", "aussi", "autre", "autrement", "avenir",
        "avec", "avancer", "avantage", "avoir", "beauté",
        "besoin", "bientôt", "bloquer", "bonheur", "bonne",
        "car", "cause", "célébrer", "certain", "chacun",
        "changement", "changer", "chaque", "choisir", "chose",
        "classe", "collaboration", "collègue", "commencer",
        "commentaire", "communication", "communauté", "complet",
        "comprendre", "confiance", "connaissance", "conseil",
        "continuer", "conversation", "correct", "correction",
        "création", "créer", "curieux", "découvrir", "déjà",
        "demander", "développer", "différence", "différent",
        "difficile", "discussion", "disponible", "document",
        "école", "économie", "écrire", "éducation", "effet",
        "effort", "élément", "élève", "émotion", "énergie",
        "engagement", "envoyer", "équipe", "erreur", "espérer",
        "essayer", "étape", "étudier", "événement", "exemple",
        "excellent", "expliquer", "expression", "facile",
        "façon", "féliciter", "fichier", "finalement", "fonction",
        "formation", "fort", "futur", "gagner", "généralement",
        "gérer", "important", "importance", "information",
        "informatique", "intéressant", "internet", "intelligent",
        "intelligence", "inviter", "logiciel", "longtemps",
        "meilleure", "mémoire", "méthode", "modérateur",
        "modératrice", "motivation", "naturel", "nécessaire",
        "objectif", "organisation", "organiser", "ordinateur",
        "outil", "parfaitement", "participer", "participation",
        "patient", "penser", "pensée", "permettre", "plaisir",
        "possible", "pratique", "préparer", "présenter",
        "présentation", "priorité", "problème", "programme",
        "progresser", "proposition", "protéger", "public",
        "qualité", "réaliser", "recevoir", "recherche",
        "recommander", "réponse", "responsable", "résultat",
        "réunion", "réussir", "réussite", "réseau", "respect",
        "respecter", "réviser", "sérieux", "service", "solution",
        "souhaiter", "spécial", "spéciale", "stratégie",
        "système", "technique", "technologie", "téléphone",
        "terminer", "texte", "thème", "utilisateur", "utiliser",
        "valeur", "vérifier", "version", "vidéo", "vocabulaire",
        "volonté", "zéro"
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
        boolean themeSombre = getSharedPreferences(
            "clavier_preferences", MODE_PRIVATE
        ).getString("theme", "clair").equals("sombre");

        clavier.setBackgroundColor(themeSombre
            ? Color.rgb(30, 32, 38)
            : Color.rgb(225, 230, 238));

        // Barre supérieure professionnelle
        LinearLayout barreSuperieure = new LinearLayout(this);
        barreSuperieure.setOrientation(LinearLayout.HORIZONTAL);
        barreSuperieure.setGravity(Gravity.CENTER_VERTICAL);
        barreSuperieure.setPadding(dp(4), 0, dp(4), 0);

        Button titreClavier = creerTouche("Mon IA");
        titreClavier.setTextSize(13);
        titreClavier.setTextColor(Color.rgb(35, 85, 160));
        ajouterTouche(barreSuperieure, titreClavier, 1.4f);
        titreClavier.setOnClickListener(v ->
            Toast.makeText(this, "Clavier IA prêt", Toast.LENGTH_SHORT).show());

        Button raccourciIA = creerTouche("IA");
        raccourciIA.setTextSize(13);
        ajouterTouche(barreSuperieure, raccourciIA, 1f);
        raccourciIA.setOnClickListener(v -> afficherMenuIA(v));

        Button raccourciQuiz = creerTouche("QUIZ");
        raccourciQuiz.setTextSize(13);
        ajouterTouche(barreSuperieure, raccourciQuiz, 1.2f);
        raccourciQuiz.setOnClickListener(v -> chargerQuestionDepuisEcran());

        Button parametres = creerTouche("⚙");
        parametres.setTextSize(17);
        ajouterTouche(barreSuperieure, parametres, 0.8f);
        parametres.setOnClickListener(v -> {
            Intent intention = new Intent(this, ParametresActivity.class);
            intention.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intention);
        });

        Button fermerClavier = creerTouche("⌄");
        fermerClavier.setTextSize(18);
        ajouterTouche(barreSuperieure, fermerClavier, 0.8f);
        fermerClavier.setOnClickListener(v -> requestHideSelf(0));

        clavier.addView(barreSuperieure,
            new LinearLayout.LayoutParams(-1, dp(38)));

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
                    0, dp(getSharedPreferences(
                        "clavier_preferences", MODE_PRIVATE
                    ).getInt("keyboard_height", 44)), 1f);
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
                            if (getSharedPreferences("clavier_preferences", MODE_PRIVATE).getBoolean("vibration", true)) {
                                vue.performHapticFeedback(
                                    android.view.HapticFeedbackConstants.KEYBOARD_TAP
                                );
                            }
                            vue.setTag(new Object[] {
                                Float.valueOf(evenement.getX()),
                                Boolean.FALSE,
                                new Handler(Looper.getMainLooper()),
                                null,
                                new boolean[] {true}
                            });
                            traiterTouche("⌫");

                            Object[] donnees = (Object[]) vue.getTag();
                            Handler gestionnaire = (Handler) donnees[2];
                            boolean[] appuye = (boolean[]) donnees[4];

                            Runnable repetition = new Runnable() {
                                @Override
                                public void run() {
                                    if (appuye[0]) {
                                        traiterTouche("⌫");
                                        gestionnaire.postDelayed(this, 70);
                                    }
                                }
                            };
                            donnees[3] = repetition;
                            gestionnaire.postDelayed(repetition, 350);
                            return true;
                        }

                        if (action == android.view.MotionEvent.ACTION_MOVE) {
                            Object objet = vue.getTag();
                            if (objet instanceof Object[]) {
                                Object[] donnees = (Object[]) objet;
                                float depart = ((Float) donnees[0]).floatValue();
                                float deplacement = evenement.getX() - depart;
                                if (deplacement < -dp(35)
                                        && !((Boolean) donnees[1]).booleanValue()) {
                                    donnees[1] = Boolean.TRUE;
                                    boolean[] appuye = (boolean[]) donnees[4];
                                    appuye[0] = false;
                                    ((Handler) donnees[2]).removeCallbacks(
                                        (Runnable) donnees[3]);
                                    effacerDernierMot();
                                }
                            }
                            return true;
                        }

                        if (action == android.view.MotionEvent.ACTION_UP
                                || action == android.view.MotionEvent.ACTION_CANCEL) {
                            Object objet = vue.getTag();
                            if (objet instanceof Object[]) {
                                Object[] donnees = (Object[]) objet;
                                ((boolean[]) donnees[4])[0] = false;
                                if (donnees[3] instanceof Runnable) {
                                    ((Handler) donnees[2]).removeCallbacks(
                                        (Runnable) donnees[3]);
                                }
                            }
                            vue.setTag(null);
                            return true;
                        }

                        return true;
                    });
                } else {
                    touche.setOnClickListener(v -> traiterTouche(symbole));

                    if (!chiffres && symbole.length() == 1
                            && Character.isLetter(symbole.charAt(0))) {
                        touche.setOnLongClickListener(v -> {
                            char lettre = majuscules
                                ? Character.toUpperCase(symbole.charAt(0))
                                : symbole.charAt(0);
                            String[] variantes = variantesAccent(lettre);
                            if (variantes.length == 0) return false;

                            PopupMenu menu = new PopupMenu(this, v);
                            for (String variante : variantes) {
                                menu.getMenu().add(variante);
                            }
                            menu.setOnMenuItemClickListener(item -> {
                                envoyer(item.getTitle().toString());
                                if (majuscules) {
                                    majuscules = false;
                                    actualiserMajuscules();
                                }
                                return true;
                            });
                            menu.show();
                            return true;
                        });
                    }
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


    private String[] variantesAccent(char lettre) {
        boolean maj = Character.isUpperCase(lettre);
        char base = Character.toLowerCase(lettre);
        String[] variantes;

        switch (base) {
            case 'a': variantes = new String[]{"à","â","ä","á","ã","å","ā","ă","ą"}; break;
            case 'b': variantes = new String[]{"ḃ","ƀ"}; break;
            case 'c': variantes = new String[]{"ç","ć","č","ĉ","ċ"}; break;
            case 'd': variantes = new String[]{"ď","đ","ḋ"}; break;
            case 'e': variantes = new String[]{"é","è","ê","ë","ē","ĕ","ė","ę","ě"}; break;
            case 'f': variantes = new String[]{"ƒ"}; break;
            case 'g': variantes = new String[]{"ĝ","ğ","ġ","ģ"}; break;
            case 'h': variantes = new String[]{"ĥ","ħ","ḧ"}; break;
            case 'i': variantes = new String[]{"î","ï","í","ì","ī","ĭ","į","ı"}; break;
            case 'j': variantes = new String[]{"ĵ"}; break;
            case 'k': variantes = new String[]{"ķ","ḱ"}; break;
            case 'l': variantes = new String[]{"ĺ","ļ","ľ","ł"}; break;
            case 'm': variantes = new String[]{"ḿ","ṁ"}; break;
            case 'n': variantes = new String[]{"ñ","ń","ņ","ň"}; break;
            case 'o': variantes = new String[]{"ô","ö","ó","ò","õ","ø","ō","ő"}; break;
            case 'p': variantes = new String[]{"ṕ","ṗ"}; break;
            case 'r': variantes = new String[]{"ŕ","ŗ","ř"}; break;
            case 's': variantes = new String[]{"ś","ŝ","ş","š"}; break;
            case 't': variantes = new String[]{"ţ","ť","ṫ","þ"}; break;
            case 'u': variantes = new String[]{"ù","û","ü","ú","ū","ŭ","ů","ű","ų"}; break;
            case 'w': variantes = new String[]{"ŵ","ẁ","ẃ","ẅ"}; break;
            case 'y': variantes = new String[]{"ÿ","ý","ŷ"}; break;
            case 'z': variantes = new String[]{"ź","ż","ž"}; break;
            default: return new String[0];
        }

        if (maj) {
            for (int i = 0; i < variantes.length; i++) {
                variantes[i] = variantes[i].toUpperCase(Locale.ROOT);
            }
        }
        return variantes;
    }

    private void traiterTouche(String touche) {
        if (touche.equals("⇧")) {
            majuscules = !majuscules;
            actualiserMajuscules();
        } else if (touche.equals("⌫")) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.beginBatchEdit();
                try {
                    CharSequence selection = ic.getSelectedText(0);
                    if (selection != null && selection.length() > 0) {
                        ic.commitText("", 1);
                    } else {
                        ic.deleteSurroundingText(1, 0);
                    }
                } finally {
                    ic.endBatchEdit();
                }
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

    private void effacerDernierMot() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        CharSequence avant = ic.getTextBeforeCursor(120, 0);
        if (avant == null || avant.length() == 0) return;

        String texte = avant.toString();
        int fin = texte.length();

        while (fin > 0 && Character.isWhitespace(texte.charAt(fin - 1))) {
            fin--;
        }

        int debut = fin;
        while (debut > 0 && Character.isLetterOrDigit(texte.charAt(debut - 1))) {
            debut--;
        }

        if (debut == fin && fin > 0) {
            debut = fin - 1;
        }

        if (fin > debut) {
            ic.deleteSurroundingText(texte.length() - debut, 0);
            actualiserSuggestions();
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

    private void afficherMenuIA(View ancre) {
        PopupMenu menu = new PopupMenu(this, ancre);
        menu.getMenu().add("Répondre");
        menu.getMenu().add("Corriger");
        menu.getMenu().add("Reformuler");
        menu.setOnMenuItemClickListener(item -> {
            String choix = item.getTitle().toString();
            String mode = choix.equals("Corriger") ? "corriger"
                : choix.equals("Reformuler") ? "reformuler" : "repondre";
            chargerQuestionDepuisPressePapiers(mode);
            return true;
        });
        menu.show();
    }

    private void chargerQuestionDepuisEcran() {
        android.content.SharedPreferences preferences =
            getSharedPreferences("clavier_preferences", MODE_PRIVATE);

        String texteEcran = preferences.getString(
            "derniere_question_ecran", "");
        long capture = preferences.getLong(
            "horodatage_question_ecran", 0L);
        long age = System.currentTimeMillis() - capture;

        if (texteEcran != null && !texteEcran.trim().isEmpty()
                && capture > 0 && age >= 0 && age <= 120000) {
            chargerQuestionDepuisPressePapiers("quiz_ecran", texteEcran);
        } else {
            Toast.makeText(this,
                "Capture écran indisponible. Copie la question puis réessaie.",
                Toast.LENGTH_LONG).show();
            chargerQuestionDepuisPressePapiers("repondre");
        }
    }

    private void chargerQuestionDepuisPressePapiers() {
        chargerQuestionDepuisPressePapiers("repondre");
    }

    private void chargerQuestionDepuisPressePapiers(String mode) {
        chargerQuestionDepuisPressePapiers(mode, null);
    }

    private void chargerQuestionDepuisPressePapiers(
            String mode, String texteForce) {
        ClipboardManager pressePapiers =
            (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        String question;

        if (texteForce != null && !texteForce.trim().isEmpty()) {
            question = texteForce.trim();
        } else {
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

            question = texte.toString().trim();
        }
        long maintenant = System.currentTimeMillis();
        if (question.equals(derniereQuestionEnvoyee)
                && mode.equals(dernierModeEnvoye)
                && maintenant - dernierHorodatageEnvoi < 1200) {
            return;
        }

        derniereQuestionEnvoyee = question;
        dernierModeEnvoye = mode;
        dernierHorodatageEnvoi = maintenant;
        final long idRequete = ++generationRequeteIA;

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
                String instruction;
                if (mode.equals("corriger")) {
                    instruction =
                        "Corrige uniquement l'orthographe, la grammaire et la ponctuation. " +
                        "Conserve le sens, le ton et la langue du texte. " +
                        "Retourne uniquement le texte corrigé, sans introduction ni Markdown. " +
                        "Texte : " + question;
                } else if (mode.equals("quiz_ecran")) {
                    instruction =
                        "Le texte suivant provient de l'écran d'une application. " +
                        "Identifie la question de quiz et ignore les menus, " +
                        "boutons et textes sans rapport. Si plusieurs questions " +
                        "sont visibles, réponds à la plus clairement posée. " +
                        "Donne uniquement la réponse utile, sans Markdown, " +
                        "sans astérisques et sans introduction. Si la question " +
                        "est ambiguë, indique-le honnêtement. Texte écran : " +
                        question;
                } else if (mode.equals("reformuler")) {
                    instruction =
                        "Reformule ce texte de manière naturelle, claire et fluide. " +
                        "Conserve fidèlement le sens et la langue d'origine. " +
                        "Retourne uniquement le texte reformulé, sans introduction ni Markdown. " +
                        "Texte : " + question;
                } else {
                    instruction =
                        "Tu es un assistant intelligent spécialisé dans les quiz, " +
                        "les connaissances générales, les définitions et les explications. " +
                        "Réponds précisément à la question. Adapte la longueur au besoin. " +
                        "N'invente pas de faits et signale honnêtement les doutes importants. " +
                        "Évite les introductions inutiles et les répétitions. " +
                        "Retourne une réponse propre, sans Markdown ni astérisques. " +
                        "Question : " + question;
                }
                demande.put("message", instruction);

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
                    if (idRequete != generationRequeteIA) return;
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
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (idRequete != generationRequeteIA) return;
                    Toast.makeText(this, "Échec : " + erreur,
                        Toast.LENGTH_LONG).show();
                });
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

    private boolean corrigerDernierMot(InputConnection ic) {
        if (ic == null) return false;

        CharSequence contenu = ic.getTextBeforeCursor(80, 0);
        if (contenu == null || contenu.length() == 0) return false;

        String avant = contenu.toString();
        int debut = avant.length();

        while (debut > 0 && Character.isLetter(avant.charAt(debut - 1))) {
            debut--;
        }

        if (debut == avant.length()) return false;

        String mot = avant.substring(debut);

        // Ne pas modifier les mots commençant par une majuscule.
        if (!mot.equals(mot.toLowerCase(Locale.ROOT))) return false;

        String corrige = mot;

        switch (mot) {
            case "bonjourr":
            case "bonjouur":
                corrige = "bonjour";
                break;
            case "mercii":
                corrige = "merci";
                break;
            case "commen":
            case "commment":
                corrige = "comment";
                break;
            case "vraimment":
                corrige = "vraiment";
                break;
            case "vraimen":
                corrige = "vraiment";
                break;
            case "beaucoups":
                corrige = "beaucoup";
                break;
            case "maintenent":
                corrige = "maintenant";
                break;
            case "aujourdhui":
                corrige = "aujourd'hui";
                break;
            case "desoler":
                corrige = "désolé";
                break;
            case "desolee":
                corrige = "désolée";
                break;
            case "saluut":
                corrige = "salut";
                break;
        }

        if (corrige.equals(mot)) return false;

        ic.deleteSurroundingText(mot.length(), 0);
        ic.commitText(corrige + " ", 1);
        return true;
    }

    private void envoyer(String texte) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            if (!texte.equals(" ") || !corrigerDernierMot(ic)) {
                ic.commitText(texte, 1);
            }
            actualiserSuggestions();
        }
    }

    private Button creerTouche(String texte) {
        Button b = new Button(this);
        b.setText(texte);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        boolean themeSombre = getSharedPreferences(
            "clavier_preferences", MODE_PRIVATE
        ).getString("theme", "clair").equals("sombre");
        b.setTextColor(themeSombre
            ? Color.rgb(240, 242, 245)
            : Color.rgb(30, 35, 45));
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
                if (getSharedPreferences(
                        "clavier_preferences", MODE_PRIVATE
                    ).getBoolean("vibration", true)) {
                    vue.performHapticFeedback(
                        android.view.HapticFeedbackConstants.KEYBOARD_TAP
                    );
                }
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
        boolean themeSombre = getSharedPreferences(
            "clavier_preferences", MODE_PRIVATE
        ).getString("theme", "clair").equals("sombre");

        GradientDrawable fond = new GradientDrawable();
        fond.setColor(themeSombre
            ? Color.rgb(54, 58, 68)
            : Color.WHITE);
        fond.setCornerRadius(dp(7));
        fond.setStroke(dp(1), themeSombre
            ? Color.rgb(75, 80, 92)
            : Color.rgb(205, 212, 222));
        return fond;
    }

    private void ajouterTouche(LinearLayout rang, Button touche, float poids) {
        int hauteurTouche = getSharedPreferences(
            "clavier_preferences", MODE_PRIVATE
        ).getInt("keyboard_height", 44);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, dp(hauteurTouche), poids);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        rang.addView(touche, params);
    }

    private int dp(int valeur) {
        return (int) (valeur * getResources().getDisplayMetrics().density + 0.5f);
    }
}
