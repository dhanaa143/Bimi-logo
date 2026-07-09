# Dhanaa Ceylon Spices — Website

Single-page site for Dhanaa Ceylon Spices LTD. Everything lives in `index.html` (no build step —
plain HTML/CSS/JS).

## Preview locally

Open `index.html` directly in a browser, or serve it so relative asset paths behave the same as
in production:

```
python3 -m http.server 8000
```

then visit `http://localhost:8000`.

## Step 1 status (mobile fix / file size / contact form)

- **Mobile bug fixed** — the page had `overflow-y: clip` on `<html>`, which froze scrolling on
  mobile Chrome (this was why nothing responded when opened on the phone). Removed.
- **File size reduced** — removed a `<video>` and `<img>` pointing at files that don't exist yet
  (`dhanaa_video.mp4`, `dhanaa_logo.png`), which were dead network requests. Replaced with
  CSS-only placeholders so the page loads clean until real media is ready.
- **Contact form** — wired to Formspree (`https://formspree.io/f/mzdllajn`, sends to
  `Dhanaaceylonspiceuk@gmail.com`). If Formspree can't be reached for any reason, it falls back to
  opening the visitor's email app with the enquiry pre-filled, so the form never fully breaks.
  **Important:** Formspree sends a confirmation email on the very first real submission — check
  the inbox and confirm it, otherwise that first submission won't be delivered.

## Adding real photos / logo

Drop files into `assets/images/` (see the README there for expected filenames) and swap the
placeholder markup at the `TODO` comments in `index.html`.

## Connecting the domain

Domain is already purchased — not connected yet. Say the word when ready and we'll wire up DNS /
hosting.
