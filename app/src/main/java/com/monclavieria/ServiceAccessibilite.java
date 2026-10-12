package com.monclavieria;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.SharedPreferences;

import java.util.LinkedHashSet;
import java.util.Set;

public class ServiceAccessibilite extends AccessibilityService {

    private static final int LIMITE_TEXTE = 6000;
    private static final int LIMITE_NOEUDS = 300;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence paquet = event.getPackageName();

        // Ne pas enregistrer les événements provenant de notre clavier.
        if (paquet != null &&
            getPackageName().contentEquals(paquet)) {
            return;
        }

        AccessibilityNodeInfo racine = getRootInActiveWindow();
        if (racine == null) return;

        StringBuilder texte = new StringBuilder();
        Set<String> dejaVus = new LinkedHashSet<>();
        int[] compteur = {0};

        lireNoeud(racine, texte, dejaVus, compteur);

        String capture = texte.toString().trim();

        // Invalider l'ancienne capture si le nouvel écran est vide.
        if (capture.length() < 15) {
            getSharedPreferences("clavier_preferences", MODE_PRIVATE)
                .edit()
                .remove("derniere_question_ecran")
                .putLong("horodatage_question_ecran", 0L)
                .apply();
            return;
        }

        if (capture.length() > LIMITE_TEXTE) {
            capture = capture.substring(0, LIMITE_TEXTE);
        }

        SharedPreferences preferences =
            getSharedPreferences("clavier_preferences", MODE_PRIVATE);

        String ancienneCapture = preferences.getString(
            "derniere_question_ecran", "");

        // Actualiser la date même si le texte de l'écran reste identique.
        if (capture.equals(ancienneCapture)) {
            preferences.edit()
                .putLong("horodatage_question_ecran",
                    System.currentTimeMillis())
                .apply();
            return;
        }

        preferences.edit()
            .putString("derniere_question_ecran", capture)
            .putLong("horodatage_question_ecran",
                System.currentTimeMillis())
            .apply();
    }

    private void lireNoeud(
        AccessibilityNodeInfo noeud,
        StringBuilder sortie,
        Set<String> dejaVus,
        int[] compteur
    ) {
        if (noeud == null ||
            compteur[0] >= LIMITE_NOEUDS ||
            sortie.length() >= LIMITE_TEXTE) {
            return;
        }

        compteur[0]++;

        ajouterTexte(noeud.getText(), sortie, dejaVus);
        ajouterTexte(noeud.getContentDescription(), sortie, dejaVus);

        for (int i = 0; i < noeud.getChildCount(); i++) {
            if (compteur[0] >= LIMITE_NOEUDS ||
                sortie.length() >= LIMITE_TEXTE) {
                break;
            }

            lireNoeud(noeud.getChild(i), sortie, dejaVus, compteur);
        }
    }

    private void ajouterTexte(
        CharSequence valeur,
        StringBuilder sortie,
        Set<String> dejaVus
    ) {
        if (valeur == null) return;

        String texte = valeur.toString()
            .replaceAll("\\s+", " ")
            .trim();

        if (texte.isEmpty() || !dejaVus.add(texte)) return;

        if (sortie.length() > 0) sortie.append('\n');

        sortie.append(texte);
    }

    @Override
    public void onInterrupt() {
        // Aucune action nécessaire.
    }
}
