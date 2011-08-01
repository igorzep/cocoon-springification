package org.alveolo.cocoon.springification.controller;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;

import org.alveolo.cocoon.springification.Record;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/record")
public class RecordController {
	@PersistenceContext
	private EntityManager em;

	private Record getOrCreateRecord(Integer id) {
		Record r = em.find(Record.class, id);
		return (r == null) ? new Record(id) : r;
	}

	@Transactional
	@RequestMapping("/{id}")
	public String get(@PathVariable Integer id, Model model) {
		model.addAttribute(getOrCreateRecord(id));
		return "view";
	}

	@Transactional
	@RequestMapping(value="/{id}/edit", method=RequestMethod.GET)
	public String edit(@PathVariable Integer id, Model model) {
		model.addAttribute(getOrCreateRecord(id));
		return "edit";
	}

	@Transactional
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	public String post(@PathVariable Integer id, @ModelAttribute("record") @Valid Record record, BindingResult result) {
		record.setId(id);

		if (!result.hasErrors()) {
			try {
				em.merge(record);
				return "redirect:/record/" + id;
			} catch (Exception e) {
				result.reject("internal", "internal error");
			}
		}

		return "edit";
	}
}
