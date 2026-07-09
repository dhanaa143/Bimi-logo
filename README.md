# Dhanaa Ceylon Spices — Website

Single-page site for Dhanaa Ceylon Spices LTD. Everything lives in `index.html` (no build step —
plain HTML/CSS/JS).

## Preview locally

Open `index.html` directly in a browser, or serve it so relative asset paths behave the same as
in production. Use a server that supports HTTP range requests so the hero video plays (Python's
built-in `http.server` does not support range requests, so the video may fail to load with it —
use `npx http-server` instead):

```
npx http-server
```

then visit the URL it prints.

## Step 1 status (mobile fix / file size / contact form)

- **Mobile bug fixed** — the page had `overflow-y: clip` on `<html>`, which froze scrolling on
  mobile Chrome (this was why nothing responded when opened on the phone). Removed.
- **File size reduced** — the hero video was re-encoded from 6.1MB (1920x1080) down to ~700KB
  (1280x720, H.264 CRF 28, audio stripped since it's muted anyway). The logo was cropped/quantized
  to two small PNGs instead of one large file.
- **Contact form** — wired to Formspree (`https://formspree.io/f/mzdllajn`, sends to
  `Dhanaaceylonspiceuk@gmail.com`). If Formspree can't be reached for any reason, it falls back to
  opening the visitor's email app with the enquiry pre-filled, so the form never fully breaks.
  **Important:** Formspree sends a confirmation email on the very first real submission — check
  the inbox and confirm it, otherwise that first submission won't be delivered.

## Adding real photos

Logo (nav + About section) and the hero background video are in place
(`assets/images/logo-mark.png`, `assets/images/logo-full.png`, `assets/video/hero-video.mp4`).
Still missing: individual product photos (cinnamon, pepper, cloves, etc. — currently emoji icons)
and a dedicated About-section photo (currently reuses the full logo). Drop files into
`assets/images/` and swap the markup at the remaining `TODO` comments in `index.html`.

## Connecting the domain

Domain is already purchased — not connected yet. Say the word when ready and we'll wire up DNS /
hosting.
