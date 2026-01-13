document.addEventListener("DOMContentLoaded", () => {

  /* ==========================
     기본 데이터 설정
  =========================== */
  const drinkData = {
    "포카리": { price: 700, stock: 5, img: "pocaOut", soldOutImg: "pocaSoldout", btnId: "poca" },
    "코카콜라": { price: 800, stock: 5, img: "colaOut", soldOutImg: "colaSoldout", btnId: "cola" },
    "환타": { price: 900, stock: 5, img: "fantaOut", soldOutImg: "fantaSoldout", btnId: "fanta" }
  };

  let totInMoney = 0;
  let VMInMoney = 0;
  let isAdmin = false;

  const purchaseCounts = { "포카리": 0, "코카콜라": 0, "환타": 0 };
  const $outDrinkImg = document.querySelector('#outDrink img');


  /* ==========================
      가격 초기 표시
  =========================== */
  for (const [name, data] of Object.entries(drinkData)) {
    document.querySelector(`#${data.btnId}A span`).textContent = data.price;
  }


  /* ==========================
      동전 드래그 & 드롭
  =========================== */
  document.querySelectorAll(".coin").forEach(coin => {
    coin.addEventListener("dragstart", e => {
      e.dataTransfer.setData("text", coin.alt);
    });
  });

  const slot = document.getElementById("slot");

  slot.addEventListener("dragover", e => e.preventDefault());

  slot.addEventListener("drop", e => {
    e.preventDefault();
    const value = parseInt(e.dataTransfer.getData("text"));

    if (!isNaN(value)) {
      totInMoney += value;
      document.getElementById("inMoney").value = totInMoney;

      const img = document.createElement("img");
      img.src = `images/coin${value}.png`;
      slot.appendChild(img);
      setTimeout(() => img.remove(), 1000);
    }
  });


  /* ==========================
      동전 직접 입력
  =========================== */
  document.getElementById("inputMoney").addEventListener("click", () => {
    const val = parseInt(document.getElementById("coin").value);
    if (!isNaN(val)) {
      totInMoney += val;
      document.getElementById("inMoney").value = totInMoney;
    }
  });


  /* ==========================
      거스름돈 배출
  =========================== */
  document.getElementById("exchangCoins").addEventListener("click", () => {
    document.getElementById("exchangeCoins").value = totInMoney;
    totInMoney = 0;
    document.getElementById("inMoney").value = 0;
  });


  /* ==========================
      음료 구매
  =========================== */
  for (const [name, data] of Object.entries(drinkData)) {

    document.getElementById(data.btnId).addEventListener("click", () => {

      if (data.stock <= 0) {
        alert(`${name}가 매진되었습니다.`);
        return;
      }

      if (totInMoney < data.price) {
        alert("투입금액이 부족합니다.");
        return;
      }

      totInMoney -= data.price;
      VMInMoney += data.price;
      data.stock--;

      document.getElementById("inMoney").value = totInMoney;

      $outDrinkImg.src = `images/${data.img}.png`;
      $outDrinkImg.style.visibility = "visible";

      updateStockDisplay();
      addPurchase(name);
      updateSalesDisplay();
    });
  }


  /* ==========================
      재고 상태 표시
  =========================== */
  function updateStockDisplay() {
    for (const [name, data] of Object.entries(drinkData)) {
      const img = document.querySelector(`img[alt="${name}"]`);
      const btn = document.getElementById(data.btnId);

      if (data.stock <= 0) {
        img.src = `images/${data.soldOutImg}.png`;
        btn.disabled = true;
      } else {
        img.src = `images/${data.img}.png`;
        btn.disabled = false;
      }
    }
  }


  /* ==========================
      구매 목록 업데이트
  =========================== */
  function addPurchase(name) {
    purchaseCounts[name]++;
    const item = document.querySelector(`#baguni div[data-name="${name}"]`);
    item.style.display = "inline-block";
    item.querySelector(".count").textContent = purchaseCounts[name];
  }


  /* ==========================
      판매 수입 표시
  =========================== */
  function updateSalesDisplay() {
    document.getElementById("totalSalesDisplay").textContent = `${VMInMoney} 원`;
  }


  /* ==========================
      관리자 모드
  =========================== */
  document.getElementById("adminToggle").addEventListener("click", () => {
    const pw = prompt("관리자 비밀번호를 입력하세요:");

    if (pw === "1234") {
      isAdmin = true;
      document.getElementById("adminPanel").style.display = "block";

      for (const [name, data] of Object.entries(drinkData)) {
        const id = data.btnId.charAt(0).toUpperCase() + data.btnId.slice(1);
        document.getElementById(`stock${id}`).value = data.stock;
        document.getElementById(`price${id}`).value = data.price;
      }
      updateSalesDisplay();
    } else {
      alert("비밀번호가 틀렸습니다.");
    }
  });

  document.getElementById("closeAdmin").addEventListener("click", () => {
    isAdmin = false;
    document.getElementById("adminPanel").style.display = "none";
  });


  /* ==========================
      관리자 설정 적용
  =========================== */
  document.getElementById("applyAdminChanges").addEventListener("click", () => {
    if (!isAdmin) return;

    for (const [name, data] of Object.entries(drinkData)) {
      const id = data.btnId.charAt(0).toUpperCase() + data.btnId.slice(1);

      data.stock = parseInt(document.getElementById(`stock${id}`).value);
      data.price = parseInt(document.getElementById(`price${id}`).value);

      document.querySelector(`#${data.btnId}A span`).textContent = data.price;
    }

    updateStockDisplay();
    alert("변경 사항이 적용되었습니다.");
  });

  updateStockDisplay();
});
