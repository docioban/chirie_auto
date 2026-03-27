# Închirieri Mașini 🚗

Aplicație Android pentru gestionarea închirierilor de mașini.

## Tema
**#16 — Închirieri mașini** (selecție, calcul preț, gestionare clienți)
**Student:** Ciobanu Dorin
**Limbaj:** Java | **Mediu:** Android Studio | **Min SDK:** 24

---

## Descriere

Aplicație mobilă care permite gestionarea completă a unui serviciu de închirieri auto:
- Catalog de mașini cu detalii complete
- Sistem de rezervări cu calcul automat al prețului
- Bază de date locală SQLite
- Interfață modernă cu animații

---

## Funcționalități

### Mașini
- Adăugare / editare / ștergere mașini
- Câmpuri: marcă, model, an fabricație, preț/zi, descriere, disponibilitate, rating
- Căutare în timp real după marcă sau model

### Închirieri
- Rezervare mașină cu selectare date (DatePicker + TimePicker)
- Opțiuni suplimentare: asigurare (+15 lei/zi), scaun copil (+10 lei), GPS (+5 lei)
- Calcul automat al prețului total
- Gestionare status: activ / finalizat
- Căutare după numele clientului sau telefon

---

## UI Elements folosite

| Element | Unde |
|---|---|
| `TextView` | Toate ecranele |
| `EditText` | Formular mașină, formular client, căutare |
| `Button` | Salvare, închiriere, selectare date |
| `RecyclerView` | Lista mașini, lista închirieri |
| `ImageView` | Logo, imagine mașină |
| `Menu` | Toolbar în MainActivity și RentalsActivity |
| `Toast` | Confirmare salvare / erori |
| `Snackbar` | Confirmare închiriere / ștergere |
| `CheckBox` | Scaun copil, GPS |
| `ToggleButton` | Disponibilitate mașină |
| `RadioButton` | Cu / fără asigurare |
| `DatePicker` | Data început și sfârșit închiriere |
| `TimePicker` | Ora ridicare |
| `SeekBar` | Rating mașină (1-5 stele) |

---

## Ecrane (Activities)

1. **MainActivity** — Lista mașini, căutare, buton adăugare (FAB)
2. **CarDetailActivity** — Detalii mașină, buton Închiriază / Editează
3. **AddEditCarActivity** — Formular adăugare sau editare mașină
4. **RentCarActivity** — Formular rezervare cu calcul preț
5. **RentalsActivity** — Lista tuturor închirierilor

---

## Baza de date (SQLite)

**Tabel `cars`:** id, brand, model, year, price_per_day, description, is_available, rating
**Tabel `rentals`:** id, car_id, client_name, client_phone, start_date, end_date, total_price, with_insurance, has_child_seat, has_gps, status

Operații implementate: **adăugare, citire, actualizare, ștergere, căutare**

---

## Transmitere date între Activities

- Obiectele `Car` și `Rental` implementează **Parcelable**
- Transmise prin `Intent.putExtra()` între toate ecranele
- Rezultate returnate cu `ActivityResultLauncher`

---

## Animații

- **Bounce** — FAB la deschiderea aplicației
- **Slide in right** — La navigarea între ecrane și iteme din RecyclerView
- **Fade in** — La încărcarea conținutului
- **Scale up** — Imaginea mașinii în detalii
- **Scale press** — La apăsarea butoanelor

---

## Tehnologii

- Java (Android SDK 34)
- SQLite + SQLiteOpenHelper
- RecyclerView + CardView
- Material Components (Material Design)
- AndroidX AppCompat
- Parcelable pentru serializare
- ActivityResultLauncher

---

## Instalare

1. Descarcă `chirie_auto.apk` din root-ul repository-ului
2. Activează **"Surse necunoscute"** pe telefon (Setări → Securitate)
3. Instalează APK-ul
