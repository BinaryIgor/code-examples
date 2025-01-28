package com.binaryigor.htmlcomponents;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
public class HtmlComponentsController {

    private static final Random RANDOM = new Random();
    private final MustacheFactory factory = new DefaultMustacheFactory();

    @GetMapping("/web-component")
    String webComponent() {
        var component = """
            class CollapsibleItem extends HTMLElement {
              connectedCallback() {
                 const header = this.getAttribute("header");
                 const items = this.getAttribute("items").split(",");
                        
                 this.innerHTML = `
                  <div>${header}</div>
                  <div style="display: none">
                  ${items.map(i => `<div>${i}</div>`).join("\\n")}
                  </div>
                 `;
                  const [itemsHeader, itemsContainer] = this.querySelectorAll("div");
                  itemsHeader.onclick = () => {
                    const itemsDisplay = itemsContainer.style.display;
                    if (itemsDisplay == 'block') {
                      itemsContainer.style.display = 'none';
                    } else {
                      itemsContainer.style.display = 'block';
                    }
                  };
              }
            }
            customElements.define("collapsible-item", CollapsibleItem);
            """;
        return html("""
              <collapsible-item
                   header="Items"
                   items="A,B">
                 </collapsible-item>
            """, component);
    }

    private String html(String body) {
        return html(body, "");
    }

    private String html(String body, String script) {
        return """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>HTML Components</title>
               </head>
               <body>
                 %s
                 %s
               </body>
            </html>
            """.formatted(body, script.isBlank() ? "" : "<script>%s</script>".formatted(script)).trim();
    }

    @GetMapping("/html-component")
    String htmlComponent() throws Exception {
        var compiled = factory.compile("collapsible.mustache");
        var writer = new StringWriter();
        compiled.execute(
                writer,
                Map.of(
                    "id", "collapsible-example",
                    "header", "Items",
                    "items", List.of("A", "B")))
            .flush();

        return html(writer.toString());
    }

    @GetMapping("/todos")
    String todos() throws Exception {
        var todos = Stream.generate(this::randomTodo)
            .limit(10)
            .toList();

        var compiled = factory.compile("todos.mustache");
        var writer = new StringWriter();
        compiled.execute(
                writer,
                Map.of("todosId", "todos",
                    "todos", todos))
            .flush();

        return html(writer.toString(), """
            (function(){
            document.getElementById("todos").onclick = (e) => {
              console.log("On todos click", e.target);
            };
            })();
            """);
    }

    private Todo randomTodo() {
        var now = LocalDateTime.now();
        return new Todo(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            RANDOM.nextBoolean(),
            now.minusMinutes(RANDOM.nextInt(24 * 60)),
            now);
    }

    record Todo(String title, String text, boolean done, LocalDateTime createdOn, LocalDateTime completedOn) {
    }
}
