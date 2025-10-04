let roomsBtn = document.getElementById("room");
let poolingBtn = document.getElementById("pooling");

let roomsSection = document.createElement("div");
roomsSection.className = "middle";
roomsSection.style.display = "none";
document.body.appendChild(roomsSection);

let poolingSection = document.createElement("div");
poolingSection.className = "pooling";
poolingSection.style.display = "none";
document.body.appendChild(poolingSection);

// Rooms UI
roomsBtn.addEventListener("click", () => {
  roomsSection.style.display = "block";
  poolingSection.style.display = "none";
  roomsSection.innerHTML = "";

  let input = document.createElement("input");
  input.type = "text";
  input.placeholder = "Search";
  input.className = "search";
  roomsSection.appendChild(input);

  let main = document.createElement("main");
  main.className = "content";

  for (let i = 0; i < 9; i++) {
    let innerDiv = document.createElement("div");
    innerDiv.className = "card";

    let span1 = document.createElement("span");
    span1.className = "room";
    span1.textContent = "10" + i;

    let span2 = document.createElement("span");
    span2.className = "share";
    span2.textContent = "Sharing";

    let anchor = document.createElement("a");
    anchor.href = "#";
    anchor.textContent = "Detail";

    let button = document.createElement("button");
    button.className = "more-btn";
    button.textContent = "More";

    innerDiv.appendChild(span1);
    innerDiv.appendChild(span2);
    innerDiv.appendChild(anchor);
    innerDiv.appendChild(button);
    main.appendChild(innerDiv);
  }

  roomsSection.appendChild(main);

  let addButton = document.createElement("button");
  addButton.className = "add";
  addButton.textContent = "Add Room +";
  roomsSection.appendChild(addButton);
});

poolingBtn.addEventListener("click", () => {
  poolingSection.style.display = "block";
  roomsSection.style.display = "none";
  poolingSection.innerHTML = "";

  let menu = document.createElement("div");
  let menu_buttons = document.createElement("ul");
  menu_buttons.className = "menu-buttons";

  let menuItems = ["Break-fast", "Lunch", "Dinner", "Pooling-Results"];
  menuItems.forEach(text => {
    let li = document.createElement("li");
    li.className = "timing";
    li.textContent = text;
    menu_buttons.appendChild(li);
  });
  menu.appendChild(menu_buttons);
  poolingSection.appendChild(menu);
  let timingLayers = {};

  menuItems.forEach(text => {
    if (text === "Pooling-Results") return;
    let layer = document.createElement("div");
    layer.className = "menu-layer";
    layer.style.display = "none";

    let ip_container = document.createElement("div");
    ip_container.className = "input-container";

    let input = document.createElement("input");
    input.type = "text";
    input.placeholder = `Enter ${text} item`;
    input.className = "Enter-item";

    let span = document.createElement("span");
    span.className = "add-icon";
    span.title = "add-item";
    span.textContent = "+";

    ip_container.appendChild(input);
    ip_container.appendChild(span);
    layer.appendChild(ip_container);

    let container = document.createElement("div");
    container.className = "container";

    let item_list = document.createElement("div");
    item_list.className = "item-list";

    let ul = document.createElement("ul");
    ul.className = "list";

    item_list.appendChild(ul);
    container.appendChild(item_list);
    layer.appendChild(container);

    let placeholderImg = document.createElement("img");
    placeholderImg.src = "https://cdn-icons-png.flaticon.com/512/1027/1027650.png";
    placeholderImg.style.width = "150px";
    placeholderImg.style.opacity = "1";
    placeholderImg.style.marginTop = "50px";
    placeholderImg.style.marginLeft = "250px";

    let addPole = document.createElement('button');
    addPole.className = "add-to-pole-btn";
    addPole.textContent = "Add-to-Pole";

    function updatePlaceholder() {
      if (ul.children.length === 0) {
        if (!container.contains(placeholderImg)) container.appendChild(placeholderImg);
        if (container.contains(addPole)) container.removeChild(addPole);
        container.style.background = "none"; 
      } else {
        if (container.contains(placeholderImg)) container.removeChild(placeholderImg);
        if (!container.contains(addPole)) item_list.appendChild(addPole);
        container.style.background = ""; 
      }
    }

    span.addEventListener("click", () => {
      let word = input.value.trim();
      if (word === "") {
        window.alert("Empty-Text");
        return;
      }

      let items = document.createElement("li");
      items.className = "items";

      let item_number = document.createElement("span");
      item_number.textContent = word;

      let remove_btn = document.createElement("img");
      remove_btn.className = "wrong";
      remove_btn.src = "https://www.svgrepo.com/show/7711/remove-button.svg";

      //when click on remove button
      remove_btn.addEventListener("click", () => {
     ul.removeChild(items);
  if (ul.children.length === 0) {
    if (item_list.contains(addPole)) {
      item_list.removeChild(addPole);
    }
    if (!container.contains(placeholderImg)) {
      container.appendChild(placeholderImg);
    }
    container.style.background = "none";
  }

  updatePlaceholder();
});
      
      items.appendChild(item_number);
      items.appendChild(remove_btn);
      ul.appendChild(items);

      input.value = "";
      updatePlaceholder();
    });

    updatePlaceholder();
    poolingSection.appendChild(layer);
    timingLayers[text] = layer;
  });

  timingLayers["Break-fast"].style.display = "block";
 menuItems.forEach(text => {
  let button = Array.from(menu_buttons.children).find(li => li.textContent === text);

  button.addEventListener("click", () => {
    Object.values(timingLayers).forEach(l => l.style.display = "none");
if (text === "Pooling-Results") {
  let resultsSection = document.querySelector(".parent-res");
  if (!resultsSection) {
    let parentDiv = document.createElement("div");
    parentDiv.className = "parent-res";

    let resultData = [
      { id: "breakfast-result", text: "Break-fast Results" },
      { id: "lunch-result", text: "Lunch Results" },
      { id: "dinner-result", text: "Dinner Results" }
    ];

    resultData.forEach(r => {
      let div = document.createElement("div");
      div.id = r.id;
      div.className = "results";
      div.textContent = r.text;

      let hr = document.createElement("hr");
      hr.className="hr-for-results"

      div.appendChild(hr);
      parentDiv.appendChild(div);
    });

    poolingSection.appendChild(parentDiv);
  }

  document.querySelector(".parent-res").style.display = "block";
  return;
}

    let results = document.querySelector(".parent-res");
    if (results) results.style.display = "none";

    timingLayers[text].style.display = "block";
  });
});
});
