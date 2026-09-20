const API_BASE = "http://localhost:8080/api/admin";


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", function () {

    loadDashboardStats();
    loadCandidates();
    loadVoters();
    loadResults();

    const candidateForm =
        document.getElementById("candidateForm");

    const voterForm =
        document.getElementById("voterForm");


    if (candidateForm) {
        candidateForm.addEventListener(
            "submit",
            addCandidate
        );
    }


    if (voterForm) {
        voterForm.addEventListener(
            "submit",
            addVoter
        );
    }

});


// =====================================================
// DASHBOARD STATS
// =====================================================

async function loadDashboardStats() {

    try {

        const response =
            await fetch(
                `${API_BASE}/stats`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load dashboard statistics"
            );

        }


        const stats =
            await response.json();


        console.log(
            "Dashboard stats:",
            stats
        );


        // Total Candidates

        const totalCandidates =
            document.getElementById(
                "totalCandidates"
            );


        if (totalCandidates) {

            totalCandidates.textContent =
                stats.totalCandidates ?? 0;

        }


        // Total Votes

        const totalVotes =
            document.getElementById(
                "totalVotes"
            );


        if (totalVotes) {

            totalVotes.textContent =
                stats.totalVotes ?? 0;

        }


        // Blockchain Blocks

        const totalBlocks =
            document.getElementById(
                "totalBlocks"
            );


        if (totalBlocks) {

            totalBlocks.textContent =
                stats.totalBlocks ?? 0;

        }


    } catch (error) {

        console.error(
            "Dashboard stats error:",
            error
        );

    }

}


// =====================================================
// LOAD CANDIDATES
// =====================================================

async function loadCandidates() {

    const list =
        document.getElementById(
            "candidateList"
        );


    if (!list) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/candidates`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load candidates"
            );

        }


        const candidates =
            await response.json();


        console.log(
            "Candidates:",
            candidates
        );


        list.innerHTML = "";


        if (
            !Array.isArray(candidates) ||
            candidates.length === 0
        ) {

            list.innerHTML = `

                <div class="management-row">

                    <div class="management-info">

                        <strong>
                            No candidates found
                        </strong>

                        <span>
                            Add a candidate using the form above.
                        </span>

                    </div>

                </div>

            `;


            updateCandidateCount(0);

            return;
        }


        candidates.forEach(
            function (candidate) {


                // Candidate ID

                const candidateId =
                    candidate.candidateId ??
                    candidate.candidate_id ??
                    candidate.id;


                // Candidate Name

                const candidateName =
                    candidate.candidateName ??
                    candidate.candidate_name ??
                    candidate.name ??
                    "Unknown Candidate";


                console.log(
                    "Candidate ID:",
                    candidateId
                );


                const row =
                    document.createElement(
                        "div"
                    );


                row.className =
                    "management-row";


                row.innerHTML = `

                    <div class="management-info">

                        <strong>
                            ${escapeHtml(candidateName)}
                        </strong>

                        <span>
                            Candidate ID: ${candidateId}
                        </span>

                    </div>


                    <div class="management-actions">

                        <button
                            type="button"
                            class="delete-btn"
                            onclick="deleteCandidate(${candidateId})">

                            Remove

                        </button>

                    </div>

                `;


                list.appendChild(row);

            }
        );


        updateCandidateCount(
            candidates.length
        );


    } catch (error) {

        console.error(
            "Candidate loading error:",
            error
        );


        list.innerHTML = `

            <div class="management-row">

                <div class="management-info">

                    <strong>
                        Unable to load candidates
                    </strong>

                    <span>
                        ${escapeHtml(error.message)}
                    </span>

                </div>

            </div>

        `;

    }

}


// =====================================================
// ADD CANDIDATE
// =====================================================

async function addCandidate(event) {

    event.preventDefault();


    const input =
        document.getElementById(
            "candidateName"
        );


    const candidateName =
        input.value.trim();


    if (!candidateName) {

        alert(
            "Please enter candidate name."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/candidates`,
                {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        candidateName:
                            candidateName
                    })

                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            alert(
                data.message ||
                "Failed to add candidate."
            );

            return;
        }


        input.value = "";


        await loadCandidates();

        await loadDashboardStats();

        await loadResults();


    } catch (error) {

        console.error(
            "Add candidate error:",
            error
        );


        alert(
            "Cannot connect to backend."
        );

    }

}


// =====================================================
// DELETE CANDIDATE
// =====================================================

async function deleteCandidate(candidateId) {

    console.log(
        "Deleting candidate ID:",
        candidateId
    );


    if (
        candidateId === undefined ||
        candidateId === null ||
        candidateId === ""
    ) {

        alert(
            "Candidate ID is missing."
        );

        return;
    }


    const confirmed =
        confirm(
            "Are you sure you want to remove this candidate?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/candidates/${candidateId}`,
                {
                    method: "DELETE"
                }
            );


        const data =
            await response.json();


        console.log(
            "Delete candidate response:",
            data
        );


        if (!response.ok) {

            alert(
                data.message ||
                "Failed to remove candidate."
            );

            return;
        }


        await loadCandidates();

        await loadDashboardStats();

        await loadResults();


    } catch (error) {

        console.error(
            "Delete candidate error:",
            error
        );


        alert(
            "Cannot connect to backend."
        );

    }

}


// =====================================================
// LOAD VOTERS
// =====================================================

async function loadVoters() {

    const list =
        document.getElementById(
            "voterList"
        );


    if (!list) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/voters`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load voters"
            );

        }


        const voters =
            await response.json();


        console.log(
            "Voters:",
            voters
        );


        list.innerHTML = "";


        if (
            !Array.isArray(voters) ||
            voters.length === 0
        ) {

            list.innerHTML = `

                <div class="management-row">

                    <div class="management-info">

                        <strong>
                            No voters found
                        </strong>

                        <span>
                            Add a voter using the form above.
                        </span>

                    </div>

                </div>

            `;

            return;
        }


        voters.forEach(
            function (voter) {


                const voterId =
                    voter.voter_id ??
                    voter.voterId ??
                    voter.id;


                const voterName =
                    voter.name ??
                    voter.voterName ??
                    "Unknown Voter";


                const hasVoted =
                    voter.has_voted ??
                    voter.hasVoted ??
                    false;


                const row =
                    document.createElement(
                        "div"
                    );


                row.className =
                    "management-row";


                row.innerHTML = `

                    <div class="management-info">

                        <strong>
                            ${escapeHtml(voterName)}
                        </strong>

                        <span>
                            Voter ID:
                            ${escapeHtml(voterId)}
                        </span>

                    </div>


                    <div class="management-actions">

                        <span
                            class="status-badge ${
                                hasVoted
                                    ? "voted"
                                    : "not-voted"
                            }">

                            ${
                                hasVoted
                                    ? "Voted"
                                    : "Not Voted"
                            }

                        </span>


                        <button
                            type="button"
                            class="delete-btn"
                            onclick="deleteVoter('${escapeJs(voterId)}')">

                            Remove

                        </button>

                    </div>

                `;


                list.appendChild(row);

            }
        );


    } catch (error) {

        console.error(
            "Voter loading error:",
            error
        );


        list.innerHTML = `

            <div class="management-row">

                <div class="management-info">

                    <strong>
                        Unable to load voters
                    </strong>

                    <span>
                        ${escapeHtml(error.message)}
                    </span>

                </div>

            </div>

        `;

    }

}


// =====================================================
// ADD VOTER
// =====================================================

async function addVoter(event) {

    event.preventDefault();


    const voterId =
        document
            .getElementById(
                "newVoterId"
            )
            .value
            .trim();


    const name =
        document
            .getElementById(
                "newVoterName"
            )
            .value
            .trim();


    const password =
        document
            .getElementById(
                "newVoterPassword"
            )
            .value
            .trim();


    if (
        !voterId ||
        !name ||
        !password
    ) {

        alert(
            "Please fill all voter details."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/voters`,
                {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        voterId:
                            voterId,

                        name:
                            name,

                        password:
                            password

                    })

                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            alert(
                data.message ||
                "Failed to add voter."
            );

            return;
        }


        document
            .getElementById(
                "newVoterId"
            )
            .value = "";


        document
            .getElementById(
                "newVoterName"
            )
            .value = "";


        document
            .getElementById(
                "newVoterPassword"
            )
            .value = "";


        await loadVoters();

        await loadDashboardStats();


    } catch (error) {

        console.error(
            "Add voter error:",
            error
        );


        alert(
            "Cannot connect to backend."
        );

    }

}


// =====================================================
// DELETE VOTER
// =====================================================

async function deleteVoter(voterId) {

    console.log(
        "Deleting voter:",
        voterId
    );


    if (
        voterId === undefined ||
        voterId === null ||
        voterId === ""
    ) {

        alert(
            "Voter ID is missing."
        );

        return;
    }


    const confirmed =
        confirm(
            "Are you sure you want to remove this voter?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/voters/${encodeURIComponent(voterId)}`,
                {
                    method: "DELETE"
                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            alert(
                data.message ||
                "Failed to remove voter."
            );

            return;
        }


        await loadVoters();

        await loadDashboardStats();


    } catch (error) {

        console.error(
            "Delete voter error:",
            error
        );


        alert(
            "Cannot connect to backend."
        );

    }

}


// =====================================================
// LOAD ELECTION RESULTS
// =====================================================

async function loadResults() {

    const container =
        document.getElementById(
            "resultsContainer"
        );


    if (!container) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE}/results`
            );


        if (!response.ok) {

            throw new Error(
                "Results endpoint unavailable"
            );

        }


        const results =
            await response.json();


        console.log(
            "Election results:",
            results
        );


        container.innerHTML = "";


        if (
            !Array.isArray(results) ||
            results.length === 0
        ) {

            container.innerHTML = `

                <div class="result-row">

                    <div class="result-info">

                        <strong>
                            No results available
                        </strong>

                        <span>
                            No candidates found.
                        </span>

                    </div>

                </div>

            `;

            return;
        }


        // Find highest vote count

        let maxVotes = 0;


        results.forEach(
            function (result) {

                const votes =
                    Number(
                        result.voteCount ??
                        result.vote_count ??
                        result.votes ??
                        0
                    );


                if (votes > maxVotes) {

                    maxVotes =
                        votes;

                }

            }
        );


        // Create result rows

        results.forEach(
            function (result) {


                const candidateName =
                    result.candidateName ??
                    result.candidate_name ??
                    result.name ??
                    "Unknown Candidate";


                const votes =
                    Number(
                        result.voteCount ??
                        result.vote_count ??
                        result.votes ??
                        0
                    );


                const percentage =
                    maxVotes > 0
                        ? (
                            votes /
                            maxVotes
                        ) * 100
                        : 0;


                const row =
                    document.createElement(
                        "div"
                    );


                row.className =
                    "result-row";


                row.innerHTML = `

                    <div class="result-info">

                        <strong>
                            ${escapeHtml(candidateName)}
                        </strong>

                        <span>
                            ${votes}
                            vote${votes === 1 ? "" : "s"}
                        </span>

                    </div>


                    <div class="result-bar">

                        <div
                            class="result-fill"
                            style="width:${percentage}%;">
                        </div>

                    </div>

                `;


                container.appendChild(row);

            }
        );


    } catch (error) {

        console.error(
            "Results loading error:",
            error
        );


        container.innerHTML = `

            <div class="result-row">

                <div class="result-info">

                    <strong>
                        Unable to load results
                    </strong>

                    <span>
                        ${escapeHtml(error.message)}
                    </span>

                </div>

            </div>

        `;

    }

}


// =====================================================
// UPDATE CANDIDATE COUNT
// =====================================================

function updateCandidateCount(count) {

    const element =
        document.getElementById(
            "totalCandidates"
        );


    if (element) {

        element.textContent =
            count;

    }

}


// =====================================================
// LOGOUT
// =====================================================

function adminLogout() {

    sessionStorage.removeItem(
        "adminLoggedIn"
    );


    window.location.href =
        "admin-login.html";

}


// =====================================================
// VIEW BLOCKCHAIN
// =====================================================

function viewBlockchain() {

    window.location.href =
        "blockchain.html";

}


// =====================================================
// HTML ESCAPE
// =====================================================

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)

        .replace(
            /&/g,
            "&amp;"
        )

        .replace(
            /</g,
            "&lt;"
        )

        .replace(
            />/g,
            "&gt;"
        )

        .replace(
            /"/g,
            "&quot;"
        )

        .replace(
            /'/g,
            "&#039;"
        );

}


// =====================================================
// JAVASCRIPT ATTRIBUTE ESCAPE
// =====================================================

function escapeJs(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)

        .replace(
            /\\/g,
            "\\\\"
        )

        .replace(
            /'/g,
            "\\'"
        );

}