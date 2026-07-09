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
- **Contact form** — wired to Formspree. It currently falls back to opening the visitor's email
  app (pre-filled) because a Formspree endpoint hasn't been configured yet. To finish this:
  1. Create a free form at [formspree.io](https://formspree.io) using
     `Dhanaaceylonspiceuk@gmail.com`.
  2. Copy the endpoint it gives you (`https://formspree.io/f/xxxxxxxx`).
  3. Paste it into `FORMSPREE_ENDPOINT` near the bottom of `index.html` (search for
     `YOUR_FORM_ID`).

## Adding real photos / logo

Drop files into `assets/images/` (see the README there for expected filenames) and swap the
placeholder markup at the `TODO` comments in `index.html`.

## Connecting the domain

Domain is already purchased — not connected yet. Say the word when ready and we'll wire up DNS /
hosting.
