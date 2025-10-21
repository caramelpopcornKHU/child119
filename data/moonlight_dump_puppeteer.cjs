const fs = require("fs/promises");
const puppeteer = require("puppeteer");

const START_URL = "https://www.e-gen.or.kr/moonlight/main.do";
const REGION_CODES = ["00","11","12","13","14","15","16","17","18","21","22","23","24","25","26","27","28","29"];

async function run() {
  const browser = await puppeteer.launch({
    headless: true,
    args: ["--no-sandbox","--disable-setuid-sandbox","--disable-dev-shm-usage"],
    defaultViewport: { width: 1440, height: 900 },
  });

  const page = await browser.newPage();
  await page.goto(START_URL, { waitUntil: "networkidle2", timeout: 120000 });

  const all = await page.evaluate(async (REGION_CODES) => {
    const sleep = (ms) => new Promise(r => setTimeout(r, ms));
    async function waitForTableUpdate(timeoutMs = 8000) {
      const start = Date.now();
      let lastHtml = "";
      while (Date.now() - start < timeoutMs) {
        const el = document.querySelector("#tabListBody");
        if (el) {
          const html = el.innerHTML.trim();
          const hasAny = html.length > 0 && !html.includes("조회덴 데이터");
          if (hasAny && html !== lastHtml) return;
          lastHtml = html;
        }
        await sleep(120);
      }
    }

    function parseRegionRows(regionCode) {
      const out = [];
      const rows = document.querySelectorAll("#tabListBody tr");
      rows.forEach(tr => {
        const tds = tr.querySelectorAll("td");
        if (tds.length < 3) return;

        const name = (tds[0]?.innerText || "").trim();
        const address = (tds[1]?.innerText || "").trim();
        const tel  = (tds[2]?.innerText || "").trim();

        let emogcode = "";
        const keyA = tr.querySelector("[data-key]");
        if (keyA) emogcode = keyA.getAttribute("data-key") || "";

        if (!emogcode) {
          const a = tr.querySelector('a[href*="jsonSearchCondition"]') || tr.querySelector("a[href]");
          if (a) {
            try {
              const u = new URL(a.href, location.origin);
              const j = u.searchParams.get("jsonSearchCondition");
              if (j) {
                const parsed = JSON.parse(j);
                if (parsed && parsed.emogcode) emogcode = parsed.emogcode;
              }
            } catch {}
          }
        }

        if (name || address || tel) out.push({ regionCode, name, address, tel, emogcode });
      });
      return out;
    }

    const results = [];
    for (const code of REGION_CODES) {
      try {
        if (window.MoonlightMain && typeof window.MoonlightMain.searchList === "function") {
          window.MoonlightMain.searchList(Number(code));
        } else {
          const tab = document.querySelector(`#tablist${code}`) ||
                      document.querySelector(`a[id^="tablist${code}"]`);
          if (tab) tab.dispatchEvent(new MouseEvent("click", { bubbles: true }));
        }
      } catch {}

      await waitForTableUpdate();
      results.push(...parseRegionRows(code));
      await sleep(200);
    }
    return results;
  }, REGION_CODES);

  await fs.writeFile("moonlight_all.json", JSON.stringify(all, null, 2), "utf8");
  console.log(`✔ JSON 저장 완료: moonlight_all.json (총 ${all.length}건)`);

  const header = ["regionCode","name","address","tel","emogcode"];
  const csvRows = [header.join(",")].concat(
    all.map(o => header.map(k => `"${String(o[k] ?? "").replaceAll('"','""')}"`).join(","))
  );
  await fs.writeFile("moonlight_all.csv", csvRows.join("\n"), "utf8");
  console.log(`✔ CSV 저장 완료: moonlight_all.csv`);

  await browser.close();
}

run().catch(err => {
  console.error("실패:", err);
  process.exit(1);
});
