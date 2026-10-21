// Small progressive enhancements; navigation and forms work without JavaScript.
const filter = document.querySelector('[data-problem-filter]');
if (filter) {
    filter.hidden = false;
    const search = filter.querySelector('input');
    const difficulty = filter.querySelector('select');
    const rows = [...document.querySelectorAll('[data-problem-row]')];
    const update = () => {
        const query = search.value.trim().toLocaleLowerCase();
        let shown = 0;
        rows.forEach(row => {
            const matches = (row.dataset.title + ' ' + row.dataset.description).toLocaleLowerCase().includes(query)
                && (!difficulty.value || row.dataset.difficulty === difficulty.value);
            row.hidden = !matches;
            if (matches) shown++;
        });
        filter.querySelector('[data-filter-count]').textContent = `${shown} of ${rows.length} problems`;
        document.querySelector('[data-no-matches]').hidden = shown !== 0 || rows.length === 0;
    };
    filter.addEventListener('submit', event => event.preventDefault());
    search.addEventListener('input', update);
    difficulty.addEventListener('change', update);
    document.querySelector('[data-clear-filter]').addEventListener('click', () => {
        search.value = ''; difficulty.value = ''; update(); search.focus();
    });
    update();
}
document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', event => {
        if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
});
const editor = document.querySelector('.code-editor');
if (editor) {
    const count = document.querySelector('[data-code-count]');
    const update = () => { count.textContent = `${editor.value.length.toLocaleString()} / 20,000 characters`; };
    editor.addEventListener('input', update);
    update();
}
