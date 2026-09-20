let selectedCandidateId = null;
let selectedCandidateName = "";


// ================= PAGE LOAD =================

window.addEventListener("DOMContentLoaded", function () {

    const voterId =
        localStorage.getItem("voterId");

    const voterName =
        localStorage.getItem("voterName");


    if (!voterId) {

        alert("Please login first.");

        window.location.href =
            "index.html";

        return;
    }


    document.getElementById(
        "welcomeText"
    ).innerText =
        "Welcome, " + voterName;


    loadCandidates();

});


// ================= LOAD CANDIDATES =================

async function loadCandidates() {

    const candidateList =
        document.getElementById(
            "candidateList"
        );


    try {

        const response =
            await fetch(
                "https://blockchain-secure-voting-system-1.onrender.com/api/candidates"
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load candidates"
            );
        }


        const candidates =
            await response.json();


        candidateList.innerHTML = "";


        candidates.forEach(
            function (candidate, index) {

                const card =
                    document.createElement(
                        "div"
                    );

                card.className =
                    "candidate-card";


                card.innerHTML = `

                    <div class="candidate-number">
                        ${index + 1}
                    </div>

                    <div class="radio"></div>

                    <h3>
                        ${candidate.candidateName}
                    </h3>

                    <p>
                        Candidate ID:
                        ${candidate.candidateId}
                    </p>

                `;


                card.onclick =
                    function () {

                        selectCandidate(
                            candidate,
                            card
                        );

                    };


                candidateList.appendChild(
                    card
                );

            }
        );


    } catch (error) {

        console.error(error);

        candidateList.innerHTML = `

            <div class="loading">

                ❌ Unable to load candidates.

                <br><br>

                Make sure the Java backend
                is running.

            </div>

        `;

    }

}


// ================= SELECT CANDIDATE =================

function selectCandidate(
    candidate,
    card
) {

    document
        .querySelectorAll(
            ".candidate-card"
        )
        .forEach(
            function (item) {

                item.classList.remove(
                    "selected"
                );

            }
        );


    card.classList.add(
        "selected"
    );


    selectedCandidateId =
        candidate.candidateId;

    selectedCandidateName =
        candidate.candidateName;


    const button =
        document.getElementById(
            "voteButton"
        );


    button.disabled = false;

    button.innerText =
        "Vote for " +
        selectedCandidateName +
        " →";

}


// ================= CONFIRM VOTE =================

function confirmVote() {

    if (
        selectedCandidateId === null
    ) {

        return;
    }


    document.getElementById(
        "selectedCandidate"
    ).innerText =
        selectedCandidateName;


    document.getElementById(
        "confirmModal"
    ).style.display =
        "flex";

}


// ================= CLOSE MODAL =================

function closeConfirm() {

    document.getElementById(
        "confirmModal"
    ).style.display =
        "none";

}


// ================= CAST VOTE =================

async function castVote() {

    const voterId =
        localStorage.getItem(
            "voterId"
        );


    if (!voterId) {

        alert(
            "Voter session expired. Please login again."
        );

        window.location.href =
            "index.html";

        return;
    }


    try {

        const response =
            await fetch(
                "https://blockchain-secure-voting-system-1.onrender.com/api/voter/vote",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        voterId:
                            voterId,

                        candidateId:
                            selectedCandidateId

                    })
                }
            );


        const data =
            await response.json();


        if (data.success) {

            alert(
                "✅ Vote Cast Successfully!\n\n" +
                "Your vote for " +
                selectedCandidateName +
                " has been recorded."
            );


            closeConfirm();


            document.getElementById(
                "voteButton"
            ).disabled = true;


            document.getElementById(
                "voteButton"
            ).innerText =
                "Vote Successfully Cast ✓";


        } else {

            alert(
                data.message
            );

        }


    } catch (error) {

        console.error(error);

        alert(
            "Cannot connect to the Java backend."
        );

    }

}


// ================= LOGOUT =================

function logout() {

    localStorage.removeItem(
        "voterId"
    );

    localStorage.removeItem(
        "voterName"
    );

    window.location.href =
        "index.html";

}


// ================= CLOSE MODAL OUTSIDE =================

window.addEventListener(
    "click",
    function (event) {

        const modal =
            document.getElementById(
                "confirmModal"
            );


        if (event.target === modal) {

            closeConfirm();

        }

    }
);