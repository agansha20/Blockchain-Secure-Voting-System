
const API_URL =
    "http://localhost:8080/api/admin";


document
    .getElementById("adminLoginForm")
    .addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            const username =
                document
                    .getElementById(
                        "adminUsername"
                    )
                    .value
                    .trim();


            const password =
                document
                    .getElementById(
                        "adminPassword"
                    )
                    .value;


            const errorMessage =
                document.getElementById(
                    "errorMessage"
                );


            errorMessage.textContent =
                "Logging in...";


            try {

                const response =
                    await fetch(
                        API_URL + "/login",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify({
                                    username:
                                        username,

                                    password:
                                        password
                                })
                        }
                    );


                const data =
                    await response.json();


                if (data.success) {

                    sessionStorage.setItem(
                        "adminLoggedIn",
                        "true"
                    );


                    window.location.href =
                        "admin.html";

                } else {

                    errorMessage.textContent =
                        data.message ||
                        "Invalid username or password.";

                }

            } catch (error) {

                console.error(error);

                errorMessage.textContent =
                    "Unable to connect to server.";

            }

        }
    );