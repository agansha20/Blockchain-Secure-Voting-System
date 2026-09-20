
const API_URL =
    "http://localhost:8080/api/voter";


async function loadBlockchain() {

    const container =
        document.getElementById(
            "blockchainContainer"
        );

    const blockCount =
        document.getElementById(
            "blockCount"
        );

    const chainStatus =
        document.getElementById(
            "chainStatus"
        );

    const validityBadge =
        document.getElementById(
            "validityBadge"
        );


    container.innerHTML = `
        <div class="loading">
            <div class="spinner"></div>
            Loading blockchain...
        </div>
    `;


    try {

        /*
         * Load blockchain blocks
         */
        const response =
            await fetch(
                API_URL + "/blockchain"
            );


        if (!response.ok) {
            throw new Error(
                "Unable to load blockchain"
            );
        }


        const blocks =
            await response.json();


        /*
         * Update block count
         */
        blockCount.textContent =
            blocks.length;


        /*
         * Display blocks
         */
        if (
            !blocks ||
            blocks.length === 0
        ) {

            container.innerHTML = `
                <div class="empty-message">
                    No blockchain blocks found.
                </div>
            `;

        } else {

            container.innerHTML = "";


            blocks.forEach(
                (block, index) => {

                    const blockElement =
                        document.createElement(
                            "div"
                        );

                    blockElement.className =
                        "block";


                    const timestamp =
                        block.timestamp ||
                        "N/A";


                    const voterId =
                        block.voterId ||
                        "N/A";


                    const candidateId =
                        block.candidateId ??
                        "N/A";


                    const previousHash =
                        block.previousHash ||
                        "N/A";


                    const hash =
                        block.hash ||
                        "N/A";


                    blockElement.innerHTML = `

                        <div class="block-header">

                            <div class="block-title">

                                <div class="block-number">
                                    #${block.index}
                                </div>

                                <h3>
                                    Block ${block.index}
                                </h3>

                            </div>

                            <div class="block-time">
                                ${timestamp}
                            </div>

                        </div>


                        <div class="block-details">

                            <div class="detail">

                                <span class="detail-label">
                                    Voter ID
                                </span>

                                <span class="detail-value">
                                    ${voterId}
                                </span>

                            </div>


                            <div class="detail">

                                <span class="detail-label">
                                    Candidate ID
                                </span>

                                <span class="detail-value">
                                    ${candidateId}
                                </span>

                            </div>


                            <div class="detail">

                                <span class="detail-label">
                                    Previous Hash
                                </span>

                                <span class="detail-value hash">
                                    ${previousHash}
                                </span>

                            </div>


                            <div class="detail">

                                <span class="detail-label">
                                    Block Hash
                                </span>

                                <span class="detail-value hash">
                                    ${hash}
                                </span>

                            </div>

                        </div>
                    `;


                    container.appendChild(
                        blockElement
                    );


                    /*
                     * Arrow between blocks
                     */
                    if (
                        index <
                        blocks.length - 1
                    ) {

                        const arrow =
                            document.createElement(
                                "div"
                            );

                        arrow.className =
                            "chain-arrow";

                        arrow.innerHTML =
                            "↓";

                        container.appendChild(
                            arrow
                        );
                    }

                }
            );
        }


        /*
         * Verify blockchain
         */
        await verifyBlockchain();


    } catch (error) {

        console.error(error);


        container.innerHTML = `
            <div class="error-message">
                Unable to load blockchain.
                <br><br>
                Make sure the Spring Boot backend
                is running on port 8080.
            </div>
        `;


        blockCount.textContent = "—";

        chainStatus.textContent =
            "Error";


        validityBadge.className =
            "validity-badge invalid";

        validityBadge.innerHTML =
            `
            <span class="status-dot"></span>
            Connection Error
            `;
    }
}


async function verifyBlockchain() {

    const chainStatus =
        document.getElementById(
            "chainStatus"
        );

    const validityBadge =
        document.getElementById(
            "validityBadge"
        );


    try {

        const response =
            await fetch(
                API_URL +
                "/blockchain/verify"
            );


        if (!response.ok) {
            throw new Error(
                "Verification failed"
            );
        }


        const data =
            await response.json();


        /*
         * Backend may return:
         * true / false
         * or { valid: true }
         */
        let valid;


        if (
            typeof data ===
            "boolean"
        ) {

            valid = data;

        } else {

            valid =
                data.valid === true ||
                data.isValid === true;
        }


        if (valid) {

            chainStatus.textContent =
                "Valid";

            chainStatus.style.color =
                "#86efac";


            validityBadge.className =
                "validity-badge valid";

            validityBadge.innerHTML =
                `
                <span class="status-dot"></span>
                Blockchain Valid
                `;

        } else {

            chainStatus.textContent =
                "Invalid";

            chainStatus.style.color =
                "#fca5a5";


            validityBadge.className =
                "validity-badge invalid";

            validityBadge.innerHTML =
                `
                <span class="status-dot"></span>
                Integrity Error
                `;
        }


    } catch (error) {

        console.error(error);


        chainStatus.textContent =
            "Unavailable";


        validityBadge.className =
            "validity-badge invalid";

        validityBadge.innerHTML =
            `
            <span class="status-dot"></span>
            Verification Failed
            `;
    }
}


/*
 * Load blockchain when page opens
 */
document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadBlockchain();

    }
);