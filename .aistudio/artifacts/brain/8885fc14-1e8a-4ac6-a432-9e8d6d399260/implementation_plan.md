# Implémentation de l'écran Catalogue & Intégration Business

Ce plan détaille la mise en place de l'écran **Catalogue** (Articles, Familles, Plateformes) et de toutes les passerelles demandées, sans toucher aux 6 sous-onglets existants, sans altération du design et sans aucun hardcode.

---

## Retours utilisateur & Décisions confirmées

> [!IMPORTANT]
> - **Navigation Catalogue** : L'écran Catalogue s'ouvre en **écran complet** avec flèche retour vers le module Business.
> - **Action sur article** : Le clic sur un article du catalogue ouvre un **dialogue modal** permettant de modifier son prix de vente / coût unitaire, ses déclinaisons et son statut masqué/actif.
> - **Sous-onglets Business** : Les 6 sous-onglets (*Aperçu, Achats, Stock, Ventes, Clients, Finances*) restent strictement inchangés (pas de 7e onglet).

---

## 1. Vue d'ensemble & Points d'entrée

1. **Bouton d'icône Catalogue dans l'en-tête Business** :
   - À côté des icônes recherche et filtre, ajout d'un bouton d'action tactile (48dp, teinte orange douce, icône `sell` / `grid_view`) ouvrant l'écran Catalogue.
2. **Bouton-carte dans l'onglet Stock** :
   - Sous les cartes de résumé KPI, ajout d'une carte pleine largeur orange douce *"Gérer le catalogue"* avec chevron, et redirection du lien *"Articles masqués (X)"* vers ce même écran.
3. **Ligne Catalogue dans l'espace "Plus"** :
   - Sous Business, ajout de la ligne *"Catalogue"* avec son icône orange et le nombre d'articles actifs.
4. **Sélecteur d'articles des formulaires d'Achat et de Vente** :
   - La première ligne de choix propose systématiquement *"+ Nouvel article"* pour créer directement un produit sans quitter le formulaire.

---

## 2. Expérience Utilisateur & Design Visuel du Catalogue

L'écran Catalogue reproduit fidèlement la maquette HTML et la capture d'écran :

1. **En-tête supérieur de contexte** :
   - Bouton retour `arrow_back` vers Business.
   - Titre *"Catalogue"*, sous-titre *"X références actives"*.
   - Boutons de recherche et filtres avancés.

2. **Sous-onglets en pilules** :
   - **Articles** (icône `inventory_2`) - vue principale.
   - **Familles** (icône `category`) - vue par familles avec compteurs et marges.
   - **Plateformes** (icône `hub`) - vue par plateformes fournisseurs (1688, Taobao, Pinduoduo).

3. **Recherche & Filtres par famille** :
   - Barre de recherche en temps réel (nom de produit, SKU / code de référence).
   - Puces de familles défilables (*Toutes (N)*, *Électronique (N)*, *Maison (N)*, *Mode (N)*, *Accessoires (N)*, et bouton *"+ Famille"*).
   - Barre secondaire : bouton *"Afficher masqués (N)"*, sélecteur de tri (*Plus vendus*, *Dernier coût*, *Nom*), et bascule vue Liste / Grille.

4. **Cartes d'articles du catalogue** :
   - Vignette produit avec badge de déclinaisons (ex. *5 vars*) et badge *Top* si applicable.
   - Badge de famille (Cyan / Violet / Orange), menu contextuel `more_vert`.
   - Nom du produit en gras, capsule SKU (ex. `TWS-P5-PRO`).
   - Pastilles de couleurs réelles et texte déclinaisons (*+2 déclinaisons*).
   - Spécifications logistiques : poids et volume (ex. *120 g · 0,0008 m³*).
   - Pied de carte : pastille stock (ex. *Stock: 23 pcs*) et dernier coût unitaire en Ariary et en Yuans (ex. *32 500 Ar (¥ 28)*).
   - Carte spécifique pour les articles masqués avec filtre grisé, icône `visibility_off`, avertissement *"N'apparaît plus aux achats"* et bouton *"Réafficher"*.

5. **Bouton d'action flottant (+ Nouvel article)** :
   - Bouton pilule Cyan/Orange *"Nouvel article"* avec icône `+`.
   - Ouvre le formulaire de création d'article avec génération de référence SKU automatique.

---

## 3. Architecture Technique & Persistance Room

- Utilisation de la table `business_orders` existante (et des entités de catalogue associées) pour consolider les références, stocks, variantes, poids, volumes, familles et plateformes sans rupture.
- Toutes les métriques (références actives, articles masqués, compteurs par famille, prix moyen et stock total) sont calculées dynamiquement dans `BusinessViewModel` via `StateFlow`.
- Synchronisation bidirectionnelle : tout nouvel article créé depuis le Catalogue apparaît immédiatement dans les formulaires d'Achat et de Vente.

---

## 4. Fichiers impactés

1. `app/src/main/java/com/example/ui/business/components/CatalogueView.kt` (créé) : Écran complet Catalogue conforme au HTML/capture (Articles, Familles, Plateformes, filtres et cartes).
2. `app/src/main/java/com/example/ui/business/BusinessUiState.kt` (modifié) : Ajout des états réactifs pour le catalogue (onglets Catalogue, filtres, mode liste/grille, familles).
3. `app/src/main/java/com/example/ui/business/BusinessViewModel.kt` (modifié) : Méthodes de recherche catalogue, tri, ajout/édition d'article, ajout de famille et bascule masqué.
4. `app/src/main/java/com/example/ui/business/BusinessScreen.kt` (modifié) : Bouton icône Catalogue orange 48dp dans l'en-tête, navigation vers l'écran Catalogue complet.
5. `app/src/main/java/com/example/ui/business/components/StockView.kt` (modifié) : Bouton-carte orange doux *"Gérer le catalogue"* sous les KPIs et redirection du lien masqué vers le Catalogue.
6. `app/src/main/java/com/example/ui/home/components/MoreModulesSheet.kt` (modifié) : Ajout de la ligne *"Catalogue"* sous Business avec icône et décompte d'articles.
7. `app/src/main/java/com/example/ui/home/HomeScreen.kt` / `MainActivity.kt` (modifié) : Branchement de la navigation vers le Catalogue depuis la feuille "Plus".
8. `app/src/main/java/com/example/ui/business/components/BusinessDialogs.kt` (modifié) : Option *"+ Nouvel article"* au début du sélecteur d'articles dans `AddOrderDialog` et `AddSaleDialog`, plus dialogues d'ajout/édition d'article du catalogue.
