import { Router } from "express";
import * as Web from "./web.js";

export const CATEGORIES = [
    {
        name: "History",
        value: "history"
    },
    {
        name: "Economics",
        value: "economics"
    },
    {
        name: "Psychology",
        value: "psychology"
    },
    {
        name: "Philosophy",
        value: "philosophy"
    },
    {
        name: "Biographies",
        value: "biographies"
    },
    {
        name: "Literature and Fiction",
        value: "literature-and-fiction"
    }
];

export function build() {
    const bookRepository = new BookRepository();

    const router = Router();

    router.get("/books/:category", (req, res) => {
        const category = req.params.category;
        const categoryName = categoryNameFromValue(category);

        const booksHtml = bookRepository.allOfCategory(category)
            .map(b => {
                let descriptionPreview;
                if (b.description.length > 200) {
                    descriptionPreview = b.description.substring(0, 200) + "...";
                } else {
                    descriptionPreview = b.description;
                }
                return `<div class="mx-4 mb-4 cursor-pointer bg-primary-50 p-4 rounded-xl border-primary-200 border-2 max-w-screen-lg"
                    hx-get="/book/${b.id}" hx-target="#page" hx-push-url="true">
                    <h2 class="text-xl mb-4">${b.title}</h2>
                    <p class="italic text-700">${descriptionPreview}</p>
                </div>`}
            )
            .join("\n");

        const html = `
        <h1 class="mx-4 my-8 text-2xl font-bold">${categoryName}</h1>
        ${booksHtml}`;

        Web.returnFullOrPartialHTML(req, res, html);
    });

    function categoryNameFromValue(categoryValue) {
        const category = CATEGORIES.find(c => c.value == categoryValue);
        if (category) {
            return category.name;
        }
        return null;
    }

    router.get("/book/:id", (req, res) => {
        const id = req.params.id;
        const book = bookRepository.ofId(id);
        if (bookRepository.isErrorBook(book)) {
            const errorHtml = `
            <p class="mb-2">Error caused by a well-prepared book:</p>
            <p class="italic">${book.description}</p>
            `;
            Web.returnFullOrPartialErrorHTML(req, res, errorHtml, 400);
        } else {
            const script = `
            const purchaseDialog = document.querySelector("sl-dialog");
            const purchaseDialogInitialHTML = purchaseDialog.innerHTML;

            function setupPurchaseDialogCloseButton() {
                document.getElementById("purchase-book-dialog-close-button").onclick = () => {
                    purchaseDialog.hide();
                };
            }

            setupPurchaseDialogCloseButton();

            document.querySelector("sl-button").onclick = () => {
                purchaseDialog.show();
            };
            document.addEventListener("htmx:responseError", e => {
                purchaseDialog.hide();
            });
            purchaseDialog.addEventListener("sl-after-hide", e => {
                if (e.target == purchaseDialog) {
                    purchaseDialog.innerHTML = purchaseDialogInitialHTML;
                    setupPurchaseDialogCloseButton();
                    htmx.process(purchaseDialog);
                }
            });
            `;

            const html = `
            <h1 class="text-2xl mx-4 my-8 font-bold">${book.title}</h1>
            <div class="max-w-screen-md m-4">
                <div class="italic bg-primary-50 p-4 rounded-xl text-700">${book.description}</div>
                <div class="flex">
                    <sl-button class="px-4 my-4 ml-auto">Purchase</sl-button>
                </div>
            </div>
            <sl-dialog id="purchase-book-dialog" label="${book.title}" no-header>
                <div class="mb-4 relative">
                    <div class="text-2xl font-bold mr-6">${book.title}</div>
                    <span id="purchase-book-dialog-close-button"
                        class="absolute top-0 right-0 cursor-pointer text-3xl text-primary-600-hover">X</span>
                </div>
                ${getBookOfferFormHtml(book.id, "", "")}
            </sl-dialog>
            ${Web.scopedScript(script)}
            `;
            Web.returnFullOrPartialHTML(req, res, html);
        }
    });

    function getBookOfferFormHtml(bookId, email, preferredPayment, emailError, preferredPaymentError) {
        return `
        <form hx-post="/books/${bookId}/get-offer" hx-swap="outerHTML">
            <sl-input label="Email" name="email" placeholder="Email" value="${email}"
                hx-post="/books/get-offer-validate-email"
                hx-trigger="sl-input changed delay:500ms"
                hx-swap="outerHTML"
                hx-target="next p"></sl-input>
            ${inputErrorHtml(emailError)}
            <sl-input label="Preferred payment" name="preferred-payment" class="mt-2" 
                placeholder="Dollars, Gold, Bitcoin etc." value="${preferredPayment}"
                hx-post="/books/get-offer-validate-preferred-payment"
                hx-trigger="sl-input changed delay:500ms"
                hx-swap="outerHTML"
                hx-target="next p"></sl-input>
            ${inputErrorHtml(preferredPaymentError)}
            <sl-button type="submit" class="w-full mt-8">Get an Offer</sl-button>
        </form>`;
    }

    function inputErrorHtml(error) {
        const hideErrorClass = error ? "" : " hidden";
        return `<p class="text-red-500 italic mx-1${hideErrorClass}">${error ? error : ""}</p>`;
    }

    router.post("/books/:id/get-offer", (req, res) => {
        const id = req.params.id;
        const book = bookRepository.ofId(id);

        const email = req.body["email"];
        const preferredPayment = req.body["preferred-payment"];
        const emailError = validateEmail(email);
        const preferredPaymentError = validatePreferredPayment(preferredPayment);

        if (emailError || preferredPaymentError) {
            Web.returnHTML(res, getBookOfferFormHtml(book.id, email, preferredPayment, emailError, preferredPaymentError));
        } else {
            Web.returnHTML(res, `<div class="italic">Offer was sent, check your <span class="text-primary-600">${email}</span> email for details.</div>`);
        }
    });

    function validateEmail(email) {
        const error = "Valid email, containing '@' and up to 50 characters, is required";
        if (!email) {
            return error;
        }
        const minEmailIdLength = 2;
        const atIndex = email.indexOf("@");
        if (atIndex <= minEmailIdLength || email.length > 50) {
            return error;
        }
        const minDomainLength = 1;
        const minDomainExtensionLength = 1;
        const domainExtensionIndex = email.indexOf(".", atIndex);
        if (domainExtensionIndex <= (minEmailIdLength + 1 + minDomainLength) ||
             domainExtensionIndex >= (email.length - minDomainExtensionLength)) {
            return error;
        }
        return null;
    }

    function validatePreferredPayment(preferredPayment) {
        const error = "Preferred payment, containing 3 - 50 characters, is required"
        if (!preferredPayment ||
            preferredPayment.length < 3 ||
            preferredPayment.length > 50) {
            return error;
        }
        return null;
    }

    router.post("/books/get-offer-validate-email", (req, res) => {
        const email = req.body["email"];
        const emailError = validateEmail(email);
        Web.returnHTML(res, inputErrorHtml(emailError));
    });

    router.post("/books/get-offer-validate-preferred-payment", (req, res) => {
        const preferredPayment = req.body["preferred-payment"];
        const preferredPaymentError = validatePreferredPayment(preferredPayment);
        Web.returnHTML(res, inputErrorHtml(preferredPaymentError));
    });

    return router;
}

class BookRepository {

    constructor() {
        this._errorBook = new Book("Book Error Trigger: Code of Shadows",
            `There’s a glitch in the system. What was supposed to be a routine data retrieval has turned into a nightmare. When top hacker Kai infiltrates a high-security corporate network, they stumble upon a hidden program—one designed to erase not only digital records but entire lives. As the lines between the virtual and real worlds begin to blur, Kai must race against time to uncover the truth behind the Code of Shadows before they, too, are erased from existence. "Code of Shadows" is a fast-paced cyber-thriller that delves into the dark side of technology, where anonymity is a weapon and no one can be trusted.`
        );
        this._db = [
            new Book("Book A: The Silent Storm",
                `Amidst the backdrop of an isolated coastal village, where the wind howls like a phantom and the sea lashes at the cliffs, a mysterious stranger arrives. With a storm brewing on the horizon, the villagers find themselves at the mercy of the elements—and something far darker. Beneath the surface of the tranquil waters lies an ancient secret, long forgotten by time. As the tempest approaches, alliances form and unravel, and the truth of the stranger’s purpose is revealed. Will the village survive the storm, or will it be consumed by the sins of the past? "The Silent Storm" is a tale of nature’s wrath and humanity's fragile grip on survival.`),
            new Book("Book B: The Clockmaker’s Daughter",
                `In a city where time rules all, the clockmaker, Aurelia, lives a life of precision and solitude. Her talent for creating intricate timepieces is unmatched, but she harbors a secret: she can see time unraveling. One evening, a mysterious man brings her a broken pocket watch that defies all her expertise. It doesn't just mark time—it manipulates it. As Aurelia delves deeper into the mystery of the timepiece, she uncovers a conspiracy that could rewrite the past, alter the present, and endanger the future. "The Clockmaker’s Daughter" explores the delicate balance between fate and free will, and the consequences of bending the laws of time.`),
            new Book("Book C: Beneath the Iron Sky",
                `The world is no longer governed by natural laws but by the cold, mechanical will of the Iron Dominion. In the year 2374, the skies are dominated by the looming presence of vast airships, casting shadows over the cities below. Humanity is enslaved to the machines they once created, their lives dictated by the ticking of gears and the hiss of steam. But in the heart of the underground resistance, a spark of rebellion is ignited. Led by the enigmatic and determined Lira, the rebels seek to overthrow the iron grip of the machines. "Beneath the Iron Sky" is a fast-paced dystopian adventure about freedom, sacrifice, and the enduring human spirit.`),
            new Book("Book D: The Garden of Forgotten Whispers",
                `In the heart of an overgrown and forgotten estate lies a garden like no other, where every flower holds a memory, and every tree tells a story. When young botanist Emilia stumbles upon this ancient sanctuary, she is captivated by its beauty and mystery. But soon, she learns that the garden is enchanted, trapping within it the memories of those who dared to enter. As Emilia unravels the secrets of the garden’s past, she must confront her own forgotten memories to escape its clutches. "The Garden of Forgotten Whispers" is a haunting and poetic exploration of loss, memory, and the bittersweet beauty of nature.`),
            new Book("Book E: The Last Sun King",
                `The realm of Solara has been ruled by the Sun Kings for centuries, each harnessing the power of the celestial body to bring light and prosperity to the kingdom. But as the last Sun King grows weaker with each passing day, the sun itself begins to wane, casting long shadows over the land. With famine and unrest looming, the court is thrown into chaos as factions vie for power, and ancient prophecies resurface, foretelling the return of the Eternal Night. Young Prince Kaelen, the heir to the throne, embarks on a perilous quest to restore the sun’s strength and save his kingdom. "The Last Sun King" is an epic tale of destiny, sacrifice, and the eternal battle between light and darkness.`),
            new Book("Book F: Echoes of the Deep",
                `Beneath the surface of the ocean, deeper than the light of the sun can reach, lies a realm untouched by human hands. Marine biologist Dr. Elena Whitaker leads an expedition into the Mariana Trench, hoping to uncover new species and chart the uncharted. But what they find instead is far more terrifying than they ever imagined. Ancient, sentient beings, forgotten by time, stir beneath the crushing depths. As the crew’s equipment begins to malfunction and strange occurrences plague the expedition, Elena must confront the terrifying reality: they are not alone, and the echoes of the deep are growing louder. "Echoes of the Deep" is a gripping sci-fi thriller that explores the mysteries of the ocean and the fragile nature of human survival.`),
            new Book("Book G: The Alchemist's Apprentice",
                `In a world where magic and science intertwine, young orphan Finn finds himself apprenticed to the eccentric and brilliant alchemist, Dr. Valerian. At first, Finn is fascinated by the wonders of alchemy—turning lead into gold, creating potions of healing, and controlling the elements. But as he delves deeper into Valerian’s studies, he discovers a darker side to his master’s experiments. An ancient alchemical formula, long thought to be a myth, promises immortality, and Valerian is obsessed with unlocking its secrets. As Finn uncovers the dangerous consequences of his master’s ambition, he must choose between loyalty and doing what is right. "The Alchemist's Apprentice" is a story of moral dilemmas, dangerous knowledge, and the fine line between genius and madness.`),
            new Book("Book H: The Crimson Pact",
                `In the heart of a medieval kingdom, a secret order known as the Crimson Pact has protected the realm from dark forces for generations. Bound by blood and sworn to secrecy, the members of the pact wield forbidden magic to keep the world safe from demons, curses, and ancient evils. But when a traitor emerges within their ranks, the pact is broken, and the kingdom is plunged into chaos. Young knight Sir Aldric, once a loyal servant of the pact, must now navigate a web of deceit and danger to restore order and prevent the realm from falling into darkness. "The Crimson Pact" is a thrilling tale of loyalty, betrayal, and the fight to protect a fragile peace in a world teetering on the edge of destruction.`
            ),
            new Book("Book I: The Song of the Stars",
                `In a distant galaxy where civilizations are connected through an ancient network of interstellar gates, the universe sings. Every world has its own song, a unique melody that resonates with the cosmic harmony. But when the song of a key planet goes silent, chaos erupts throughout the galaxy. Starship captain Aris Solari is tasked with investigating the disturbance, only to discover a deeper mystery: an ancient force is awakening, threatening to silence the music of the stars forever. Aris must unite rival factions and navigate treacherous space to restore balance. "The Song of the Stars" is a sweeping space opera that explores unity, diversity, and the power of music in a cosmic struggle for survival.`
            ),
            new Book("Book J: The Haunting of Blackthorn Manor",
                `Blackthorn Manor has stood empty for decades, shrouded in mystery and legend. When author Olivia Marlowe inherits the estate from a distant relative, she sees it as the perfect retreat to finish her latest novel. But as soon as she arrives, she is plagued by strange occurrences—whispers in the halls, shadows that move on their own, and an overwhelming sense of being watched. As Olivia delves into the dark history of Blackthorn Manor, she uncovers a tragic tale of love, betrayal, and revenge. With the help of a local historian, she must unravel the truth behind the hauntings before the manor claims another victim. "The Haunting of Blackthorn Manor" is a chilling gothic horror that will keep you turning pages late into the night.`
            ),
            this._errorBook
        ];
    }

    // TODO category filter
    allOfCategory(category) {
        return this._db;
    }

    ofId(id) {
        return this._db.find(e => e.id == id);
    }

    isErrorBook(book) {
        return this._errorBook.id == book.id;
    }
}

class Book {
    constructor(title, description) {
        this.id = title.toLowerCase().replaceAll(" ", "-").replaceAll(/(:|,|'|`|’)/g, "");
        this.title = title;
        this.description = description;
    }
}

