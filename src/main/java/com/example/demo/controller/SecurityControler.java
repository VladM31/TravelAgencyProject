package com.example.demo.controller;

import com.example.demo.Test;
import com.example.demo.dao.idao.IDAOCustomer;
import com.example.demo.entity.Customer;
import com.example.demo.entity.User;
import com.example.demo.forms.ChooseSignUpForm;
import com.example.demo.forms.CustomerForm;
import com.example.demo.forms.TravelAgencyForm;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class SecurityControler {

    private IDAOCustomer<Customer> datadao;

    public SecurityControler() {
        try(ClassPathXmlApplicationContext context =
                    new ClassPathXmlApplicationContext("DAOContext.xml")) {
            datadao = context.getBean("DAOCustomer",IDAOCustomer.class);
        } finally {
        }
        System.out.println(datadao);
    }

    @RequestMapping(value = { "/", "/mainWindow" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String showMainWindow(Model model) {

        return "mainWindowPage";
    }

    @RequestMapping(value = { "/login"}, method = { RequestMethod.GET })
    public String signInGet(Model model) {
        model.addAttribute("tes", new Test(false,true));
        model.addAttribute("user",new User());
        return "login";
    }

    @RequestMapping(value = { "/login"}, method = { RequestMethod.POST })
    public String signInPOST(Model model, User user) {


        User temp = this.datadao.findByUsername(user.getUsername());
        if(temp == null || (!temp.getPassword().equals(user.getPassword()))){
            model.addAttribute("tes", new Test(true,false));
            model.addAttribute("user",user);
            return "login";
        }

        System.out.println("//-=-=-=- Sign in post -=-=-=-");
        System.out.println(user.getUsername());
        System.out.println(user.getPassword());
        System.out.println("\\\\-=-=-=- Sign in post -=-=-=-");

        return "redirect:/mainWindow";
    }

    @RequestMapping(value= {"/sign_up"},method = { RequestMethod.GET })
    public String signUpGet(Model model)
    {
        model.addAttribute("setSignUp",new ChooseSignUpForm(null,false,true));

        return "choose_sign_upPage";
    }

    @RequestMapping(value= {"/sign_up"},method = { RequestMethod.POST })
    public String signUpPOST(Model model,ChooseSignUpForm form)
    {
        if (form.isEmpty()) {
            model.addAttribute("setSignUp", new ChooseSignUpForm(null, true, false));
            return "choose_sign_upPage";
        }

        if (form.isCustomer()) {
            model.addAttribute("customer",new CustomerForm());
            return "sign_up_customerPage";
        }else{
            model.addAttribute("travel",new TravelAgencyForm());
            return "sign_up_travel_agencyPage";
        }
    }

    @RequestMapping(value= {"/sign_up_error"},method = { RequestMethod.POST })
    public String signUpCustomerPOST(Model model, CustomerForm form)
    {

        if(!form.hasGender())
        {
            form.turnOnError();
            model.addAttribute("customer",form);
            return "sign_up_customerPage";
        }

        this.datadao.save(form.getCustomer());

        return "redirect:/login";
    }

    @RequestMapping(value= {"/sign_up_error_travel"},method = { RequestMethod.POST })
    public String signUpTravelAgencyPOST(Model model, TravelAgencyForm form)
    {
        if (form.isChooseEmpty())
        {
            model.addAttribute("travel",form.getErrorForm());
            return "sign_up_travel_agencyPage";
        }
        System.out.println(form);
        return "redirect:/login";
    }
}