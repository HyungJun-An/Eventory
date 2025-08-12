// src/components/MainLayout.jsx
import React from "react";
import Navbar from "./Navbar"
import { Outlet } from "react-router-dom";

export default function MainLayout({ children }) {
    return (
        <>
            <Navbar/>
            <main>
                <Outlet />
            </main>
        </>
    );
}