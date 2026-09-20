
// ================= LOGIN =================

function openLogin(type) {

    const modal =
        document.getElementById("loginModal");

    const title =
        document.getElementById("loginTitle");

    const voterId =
        document.getElementById("voterId");

    const password =
        document.getElementById("password");


    if (type === "admin") {

        title.innerText = "Admin Login";

        voterId.placeholder =
            "Enter admin username";

    } else {

        title.innerText = "Voter Login";

        voterId.placeholder =
            "Enter your voter ID";
    }


    voterId.value = "";

    password.value = "";

    modal.style.display = "flex";
}


// ================= CLOSE LOGIN =================

function closeLogin() {

    document.getElementById(
        "loginModal"
    ).style.display = "none";
}


// ================= VOTER LOGIN =================

async function loginUser() {

    const voterId =
        document.getElementById(
            "voterId"
        ).value.trim();

    const password =
        document.getElementById(
            "password"
        ).value.trim();


    // Check empty fields
    if (
        voterId === "" ||
        password === ""
    ) {

        alert(
            "Please enter your Voter ID and Password."
        );

        return;
    }


    try {

        // Send login request to Spring Boot
        const response =
            await fetch(
                "http://localhost:8080/api/voter/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        voterId:
                            voterId,

                        password:
                            password

                    })
                }
            );


        // Convert response to JSON
        const data =
            await response.json();


        // ================= LOGIN SUCCESS =================

        if (data.success) {

            // Save voter information
            // These values are used by voting.html

            localStorage.setItem(
                "voterId",
                data.voterId
            );

            localStorage.setItem(
                "voterName",
                data.name
            );

            localStorage.setItem(
                "hasVoted",
                data.hasVoted
            );


            // Close login popup
            closeLogin();


            // ================= ALREADY VOTED =================

            if (data.hasVoted) {

                alert(
                    "Welcome " +
                    data.name +
                    "!\n\n" +
                    "You have already voted."
                );


                console.log(
                    "Voter already voted:",
                    data
                );


                return;
            }


            // ================= NEW VOTER =================

            alert(
                "Welcome " +
                data.name +
                "!\n\n" +
                "Login successful!"
            );


            console.log(
                "Voter login successful:",
                data
            );


            // Go to voting page
            window.location.href =
                "voting.html";


        } else {

            // ================= LOGIN FAILED =================

            alert(
                data.message
            );
        }


    } catch (error) {

        console.error(
            "Login Error:",
            error
        );


        alert(
            "Cannot connect to the Java backend.\n\n" +
            "Please make sure Spring Boot is running."
        );
    }
}


// ================= FEATURES =================

function scrollToFeatures() {

    document.getElementById(
        "features"
    ).scrollIntoView({
        behavior: "smooth"
    });
}


// ================= CLOSE MODAL =================

window.addEventListener(
    "click",
    function(event) {

        const modal =
            document.getElementById(
                "loginModal"
            );


        if (event.target === modal) {

            closeLogin();
        }

    }
);
