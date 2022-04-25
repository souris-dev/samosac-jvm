window.addEventListener("theme-switch", (event) => {
  if (event.data == "dark") {
    jtd.setTheme("dark");
  }
  else if (event.data == "light") {
    jtd.setTheme("light");
  }
}, false);