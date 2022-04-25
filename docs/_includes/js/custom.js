window.addEventListener("message", (event) => {
  if (event.data == "theme-switch-dark") {
    jtd.setTheme("dark");
  }
  else if (event.data == "theme-switch-light") {
    jtd.setTheme("light");
  }
}, false);