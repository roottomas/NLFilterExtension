import React from "react";
import { createRoot } from "react-dom/client";

import 'primereact/resources/themes/lara-light-green/theme.css';
import 'primereact/resources/primereact.min.css';
import { NLQueryPopup } from "./nlQueryPopup.tsx";

function mount() {
    const rootEl = document.getElementById("root");
    if (!rootEl) return;

    createRoot(rootEl).render(<NLQueryPopup/>);
}

mount();
