import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// GitHub Pages serves this repo at https://<owner>.github.io/personal-website/, so the
// production build needs a matching basePath. Local dev and Docker builds are unaffected.
const isGithubPagesBuild = process.env.GITHUB_PAGES === "true";
const repoBasePath = "/personal-website";

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  turbopack: {
    root: __dirname,
  },
  ...(isGithubPagesBuild && {
    output: "export",
    basePath: repoBasePath,
    images: { unoptimized: true },
  }),
};

export default nextConfig;
