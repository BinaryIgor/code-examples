package com.binaryigor.htmxproductionsetup.shared.web;

import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    //TODO: improve!
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            var statusCode = Integer.parseInt(status.toString());
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return HTMX.fullPage("<h1>Not Found</h1>", true);
            }
        }

        return  HTMX.fullPage("<h1>Unknown error, should never happen</h1>", true);
    }
}
